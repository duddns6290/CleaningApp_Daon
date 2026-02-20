package com.org.cleaningapp_daon.chat.dto;

import java.time.Instant;

public record ChatRoomResponse(
        Long roomId,
        Long requestId,
        Long customerId,
        Long providerId,
        String lastMessage,
        Instant lastMessageAt
) {}