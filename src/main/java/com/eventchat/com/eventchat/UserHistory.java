package com.eventchat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class UserHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String action;
    private LocalDateTime timestamp;

    public UserHistory() {}

    public UserHistory(String username, String action) {
        this.username = username;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
