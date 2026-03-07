package com.org.cleaningapp_daon.chat.dto;

import com.org.cleaningapp_daon.chat.entity.ChatMessage;

import java.time.Instant;

public record ChatMessageResponse(
        Long id,
        Long roomId,
        String senderId,
        ChatMessage.SenderRole senderRole,
        String message,
        boolean readYn,
        Instant createdAt
) {
    public static ChatMessageResponse from(ChatMessage m) {
        return new ChatMessageResponse(
                m.getId(),
                m.getRoomId(),
                m.getSenderId(),
                m.getSenderRole(),
                m.getMessage(),
                m.isReadYn(),
                m.getCreatedAt()
        );
    }
}