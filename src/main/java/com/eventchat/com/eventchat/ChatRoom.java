package com.eventchat;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private boolean isPrivate;
    private String password;

    public ChatRoom() {}

    public ChatRoom(String name, boolean isPrivate, String password) {
        this.name = name;
        this.isPrivate = isPrivate;
        this.password = password;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public boolean isPrivate() { return isPrivate; }
    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }
}