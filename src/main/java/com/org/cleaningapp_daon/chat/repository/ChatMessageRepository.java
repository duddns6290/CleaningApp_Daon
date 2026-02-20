package com.org.cleaningapp_daon.chat.repository;

import com.org.cleaningapp_daon.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    long countByRoomIdAndReadYnFalseAndSenderIdNot(Long roomId, Long myId);
}