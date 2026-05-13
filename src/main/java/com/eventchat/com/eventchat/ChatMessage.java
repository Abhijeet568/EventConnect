package com.eventchat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private String sender;
    private String content;
    private String tag;
    private String status;
    private String replyToUser;
    private String replyToMessage;

    // --- NEW MEDIA & DELETE FEATURES ---
    private String fileUrl;
    private String fileType; // e.g., "image", "video", "file"
    private boolean isDeleted = false; // For "Delete for Everyone"

    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(String roomId, String sender, String content, String tag, String status, String replyToUser, String replyToMessage, String fileUrl, String fileType) {
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.tag = tag;
        this.status = status;
        this.replyToUser = replyToUser;
        this.replyToMessage = replyToMessage;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getRoomId() { return roomId; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTag() { return tag; }
    public String getStatus() { return status; }
    public String getReplyToUser() { return replyToUser; }
    public String getReplyToMessage() { return replyToMessage; }
    public String getFileUrl() { return fileUrl; }
    public String getFileType() { return fileType; }
    public boolean isDeleted() { return isDeleted; }

    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public void setContent(String content) { this.content = content; }
}