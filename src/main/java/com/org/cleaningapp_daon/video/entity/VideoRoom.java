package com.org.cleaningapp_daon.video.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(
        name = "video_room",
        uniqueConstraints = @UniqueConstraint(name = "uk_video_room_chat_room", columnNames = "chatRoomId")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class VideoRoom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chatRoomId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public boolean isParticipant(String userId) {
        return userId != null && (userId.equals(customerId) || userId.equals(providerId));
    }
}