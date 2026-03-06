package com.org.cleaningapp_daon.chat.dto;

/**
 * 채팅방 상세 (헤더 제목용: 상대방 회원명 표시)
 */
public record ChatRoomDetailResponse(
        Long roomId,
        String otherUserId,
        String otherUserName
) {}
