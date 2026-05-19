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
    private String creator; // NEW FIELD

    public ChatRoom() {}

    public ChatRoom(String name, boolean isPrivate, String password, String creator) {
        this.name = name;
        this.isPrivate = isPrivate;
        this.password = password;
        this.creator = creator;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getPassword() {
        return password;
    }

    public String getCreator() {
        return creator;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
    
}