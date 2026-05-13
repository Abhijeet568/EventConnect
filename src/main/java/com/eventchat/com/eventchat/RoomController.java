package com.eventchat;

import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final ChatRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository; // Added to delete messages

    // We now inject both repositories
    public RoomController(ChatRoomRepository roomRepository, ChatMessageRepository messageRepository) {
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
    }

    @PostConstruct
    public void initRooms() {
        if (roomRepository.count() == 0) {
            roomRepository.save(new ChatRoom("General Chat", false, null));
            roomRepository.save(new ChatRoom("Java Developers", false, null));
            roomRepository.save(new ChatRoom("Top Secret Club", true, "admin123"));
        }
    }

    @GetMapping
    public List<Map<String, String>> getRooms() {
        List<Map<String, String>> safeRooms = new ArrayList<>();
        for (ChatRoom room : roomRepository.findAll()) {
            Map<String, String> safeRoom = new HashMap<>();
            safeRoom.put("id", room.getId().toString());
            safeRoom.put("name", room.getName());
            safeRoom.put("isPrivate", String.valueOf(room.isPrivate()));
            safeRooms.add(safeRoom);
        }
        return safeRooms;
    }

    @PostMapping("/verify")
    public boolean verifyPassword(@RequestBody Map<String, String> payload) {
        Long roomId = Long.parseLong(payload.get("roomId"));
        String password = payload.get("password");
        Optional<ChatRoom> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            ChatRoom room = roomOpt.get();
            if (!room.isPrivate()) return true;
            return password != null && password.equals(room.getPassword());
        }
        return false;
    }

    @PostMapping("/update-password")
    public boolean updatePassword(@RequestBody Map<String, String> payload) {
        Long roomId = Long.parseLong(payload.get("roomId"));
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        Optional<ChatRoom> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            ChatRoom room = roomOpt.get();
            if (room.isPrivate() && oldPassword.equals(room.getPassword())) {
                room.setPassword(newPassword);
                roomRepository.save(room);
                return true;
            }
        }
        return false;
    }

    // --- NEW ADMIN FEATURES ---

    // 1. Create a new Room
    @PostMapping("/create")
    public boolean createRoom(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        boolean isPrivate = Boolean.parseBoolean(payload.get("isPrivate"));
        String password = payload.get("password");

        // Validate data
        if (name == null || name.trim().isEmpty()) return false;
        if (isPrivate && (password == null || password.trim().isEmpty())) return false;

        roomRepository.save(new ChatRoom(name, isPrivate, password));
        return true;
    }

    // 2. Delete a Room and all its messages
    @DeleteMapping("/{id}")
    public boolean deleteRoom(@PathVariable Long id) {
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            messageRepository.deleteByRoomId(id.toString()); // Clean up old messages!
            return true;
        }
        return false;
    }
}