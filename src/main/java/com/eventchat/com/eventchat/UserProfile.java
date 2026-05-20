package com.eventchat;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Entity
public class UserProfile {

    @Id
    private String username;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    // OTP Fields
    private String otpCode;
    private LocalDateTime otpExpiry;

    private String bio = "Software Developer | Event Organizer";
    private String joinedDate;

    private int roomsCreated = 0;
    private int messagesSent = 0;
    private int eventsJoined = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> connections = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> joinedRooms = new HashSet<>();

    public UserProfile() {}

    // --- FIX: Restored this constructor to resolve the IntelliJ Error ---
    public UserProfile(String username) {
        this.username = username;
        this.joinedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));
    }

    // Constructor used by the new Authentication system
    public UserProfile(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.joinedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));
    }

    // Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    public LocalDateTime getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime otpExpiry) { this.otpExpiry = otpExpiry; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getJoinedDate() { return joinedDate; }
    public void setJoinedDate(String joinedDate) { this.joinedDate = joinedDate; }
    public int getRoomsCreated() { return roomsCreated; }
    public void setRoomsCreated(int roomsCreated) { this.roomsCreated = roomsCreated; }
    public int getEventsJoined() { return eventsJoined; }
    public void setEventsJoined(int eventsJoined) { this.eventsJoined = eventsJoined; }
    public int getMessagesSent() { return messagesSent; }
    public void setMessagesSent(int messagesSent) { this.messagesSent = messagesSent; }
    public Set<String> getConnections() { return connections; }
    public void setConnections(Set<String> connections) { this.connections = connections; }
    public Set<String> getJoinedRooms() { return joinedRooms; }
    public void setJoinedRooms(Set<String> joinedRooms) { this.joinedRooms = joinedRooms; }
}