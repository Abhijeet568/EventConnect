package com.eventchat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomIdOrderByTimestampAsc(String roomId);

    // NEW: Deletes all messages when a room is destroyed!
    @Transactional
    void deleteByRoomId(String roomId);
}