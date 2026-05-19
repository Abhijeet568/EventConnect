package com.eventchat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageRepository chatRepository;
    private final UserProfileRepository userProfileRepo;
    private final UserHistoryRepository historyRepo;
    private final ChatRoomRepository roomRepo;

    private final Map<String, Set<WebSocketSession>> activeConnections = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // UPDATED: Constructor with all new repositories injected
    public ChatWebSocketHandler(ChatMessageRepository chatRepository,
                                UserProfileRepository userProfileRepo,
                                UserHistoryRepository historyRepo,
                                ChatRoomRepository roomRepo) {
        this.chatRepository = chatRepository;
        this.userProfileRepo = userProfileRepo;
        this.historyRepo = historyRepo;
        this.roomRepo = roomRepo;
    }

    public int getActiveMemberCount(String roomId) {
        Set<WebSocketSession> sessions = activeConnections.get(roomId);
        if (sessions == null) return 0;

        int count = 0;
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) count++;
        }
        return count;
    }

    private String getRoomId(WebSocketSession session) { return session.getUri().getPath().split("/")[2]; }
    private String getUsername(WebSocketSession session) { return session.getUri().getPath().split("/")[3]; }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomId(session);
        String username = getUsername(session);

        activeConnections.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

        // --- NEW: Update User Profile & History for Joined Rooms ---
        userProfileRepo.findById(username).ifPresent(profile -> {
            try {
                Optional<ChatRoom> r = roomRepo.findById(Long.parseLong(roomId));
                if(r.isPresent()) {
                    String rName = r.get().getName();
                    if(profile.getJoinedRooms().add(rName)) {
                        historyRepo.save(new UserHistory(username, "Joined event: " + rName));
                    }
                }
                userProfileRepo.save(profile);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Could not parse room ID for history tracking: " + roomId);
            }
        });

        // Load chat history
        List<ChatMessage> history = chatRepository.findByRoomIdOrderByTimestampAsc(roomId);
        for (ChatMessage msg : history) {
            session.sendMessage(new TextMessage(createJsonMessage("CHAT", msg.getId(), msg.getSender(), msg.getContent(), msg.getTag(), msg.getStatus(), msg.getReplyToUser(), msg.getReplyToMessage(), msg.getFileUrl(), msg.getFileType(), msg.isDeleted())));
        }

        // Broadcast JOIN message
        broadcast(createJsonMessage("SYSTEM", null, "System", "🟢 " + username + " joined the room", "", "", "", "", null, null, false), roomId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String roomId = getRoomId(session);
            String username = getUsername(session);
            JsonNode incomingJson = objectMapper.readTree(message.getPayload());

            String type = incomingJson.has("type") ? incomingJson.get("type").asText() : "CHAT";

            if ("DELETE".equals(type)) {
                Long messageId = incomingJson.get("messageId").asLong();
                Optional<ChatMessage> optMsg = chatRepository.findById(messageId);

                if (optMsg.isPresent() && optMsg.get().getSender().equals(username)) {
                    ChatMessage msgToUpdate = optMsg.get();
                    msgToUpdate.setDeleted(true);
                    msgToUpdate.setContent("🚫 This message was deleted");
                    chatRepository.save(msgToUpdate);

                    broadcast(createJsonMessage("DELETE_UPDATE", messageId, username, "🚫 This message was deleted", "", "", "", "", null, null, true), roomId);
                }
                return;
            }

            // --- NEW: Update Messages Sent Stat ---
            userProfileRepo.findById(username).ifPresent(profile -> {
                profile.setMessagesSent(profile.getMessagesSent() + 1);
                userProfileRepo.save(profile);
            });

            String content = incomingJson.get("content").asText();
            String tag = incomingJson.has("tag") ? incomingJson.get("tag").asText() : "";
            String replyToUser = incomingJson.has("replyToUser") ? incomingJson.get("replyToUser").asText() : "";
            String replyToMessage = incomingJson.has("replyToMessage") ? incomingJson.get("replyToMessage").asText() : "";
            String fileUrl = incomingJson.has("fileUrl") ? incomingJson.get("fileUrl").asText() : null;
            String fileType = incomingJson.has("fileType") ? incomingJson.get("fileType").asText() : null;
            String status = "✓✓";

            ChatMessage chatMessage = new ChatMessage(roomId, username, content, tag, status, replyToUser, replyToMessage, fileUrl, fileType);
            chatRepository.save(chatMessage);

            broadcast(createJsonMessage("CHAT", chatMessage.getId(), username, content, tag, status, replyToUser, replyToMessage, fileUrl, fileType, false), roomId);

        } catch (Exception e) {
            System.out.println("⚠️ Ignored a bad message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = getRoomId(session);
        String username = getUsername(session);
        Set<WebSocketSession> roomSessions = activeConnections.get(roomId);

        if (roomSessions != null) {
            roomSessions.remove(session);
            if (roomSessions.isEmpty()) {
                activeConnections.remove(roomId);
            }
        }

        // Broadcast LEFT message
        broadcast(createJsonMessage("SYSTEM", null, "System", "🔴 " + username + " left the room", "", "", "", "", null, null, false), roomId);
    }

    private void broadcast(String jsonMessage, String roomId) throws IOException {
        Set<WebSocketSession> roomSessions = activeConnections.get(roomId);
        if (roomSessions != null) {
            for (WebSocketSession session : roomSessions) {
                if (session.isOpen()) { session.sendMessage(new TextMessage(jsonMessage)); }
            }
        }
    }

    private String createJsonMessage(String type, Long id, String sender, String content, String tag, String status, String replyUser, String replyMsg, String fileUrl, String fileType, boolean isDeleted) throws Exception {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", type);
        if (id != null) node.put("id", id);
        node.put("sender", sender); node.put("content", content); node.put("tag", tag); node.put("status", status);
        node.put("replyToUser", replyUser != null ? replyUser : ""); node.put("replyToMessage", replyMsg != null ? replyMsg : "");
        if (fileUrl != null) node.put("fileUrl", fileUrl);
        if (fileType != null) node.put("fileType", fileType);
        node.put("isDeleted", isDeleted);
        return objectMapper.writeValueAsString(node);
    }
}