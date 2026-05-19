package com.eventchat;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Entity
public class UserProfile {

    @Id
    private String username;
    private String bio = "Software Developer | Event Organizer";
    private String joinedDate;

    private int roomsCreated = 0;
    private int messagesSent = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> connections = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> joinedRooms = new HashSet<>();

    public UserProfile() {}

    public UserProfile(String username) {
        this.username = username;
        this.joinedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));
    }

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getBio() { return bio; }
    public String getJoinedDate() { return joinedDate; }
    public int getRoomsCreated() { return roomsCreated; }
    public void setRoomsCreated(int roomsCreated) { this.roomsCreated = roomsCreated; }
    public int getMessagesSent() { return messagesSent; }
    public void setMessagesSent(int messagesSent) { this.messagesSent = messagesSent; }
    public Set<String> getConnections() { return connections; }
    public Set<String> getJoinedRooms() { return joinedRooms; }
}