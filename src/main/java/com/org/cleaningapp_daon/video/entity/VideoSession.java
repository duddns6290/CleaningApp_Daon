package com.org.cleaningapp_daon.video.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "video_session", indexes = {
        @Index(name = "idx_video_session_room_status", columnList = "videoRoomId,status")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class VideoSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long videoRoomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // 누가 통화를 시작했는지
    @Column(nullable = false)
    private String createdBy;

    private Instant ringingAt;
    private Instant acceptedAt;
    private Instant endedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public enum Status {
        CREATED,   // 생성됨
        RINGING,   // 수신 대기
        ACTIVE,    // 통화중
        REJECTED,  // 거절됨
        MISSED,    // 부재중
        ENDED      // 정상 종료
    }
}