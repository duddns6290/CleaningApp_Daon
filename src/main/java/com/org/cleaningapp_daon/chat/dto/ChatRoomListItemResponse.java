package com.org.cleaningapp_daon.chat.dto;

import java.time.Instant;

/**
 * 채팅방 목록 한 건 (상대방 회원명, 안 읽은 개수 포함)
 */
public record ChatRoomListItemResponse(
        Long id,
        Long requestId,
        String customerId,
        String providerId,
        String lastMessage,
        Instant lastMessageAt,
        Instant createdAt,
        String otherUserId,
        String otherUserName,
        long unreadCount
) {}
