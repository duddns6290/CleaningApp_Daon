package com.org.cleaningapp_daon.video.dto;

import com.org.cleaningapp_daon.video.entity.VideoSession;
import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class VideoSessionResponse {
    private Long sessionId;
    private Long chatRoomId;
    private VideoSession.Status status;
    private String createdBy;
    private Instant createdAt;
    private Instant acceptedAt;
    private Instant endedAt;

    public static VideoSessionResponse of(Long chatRoomId, VideoSession s) {
        return VideoSessionResponse.builder()
                .sessionId(s.getId())
                .chatRoomId(chatRoomId)
                .status(s.getStatus())
                .createdBy(s.getCreatedBy())
                .createdAt(s.getCreatedAt())
                .acceptedAt(s.getAcceptedAt())
                .endedAt(s.getEndedAt())
                .build();
    }
}