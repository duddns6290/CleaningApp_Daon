package com.org.cleaningapp_daon.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "chat_room",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_room_request", columnNames = "requestId"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 청소 요청 id (1요청 = 1채팅방)
    @Column(nullable = false)
    private Long requestId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long providerId;

    private String lastMessage;
    private Instant lastMessageAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public boolean isParticipant(Long userId) {
        return customerId.equals(userId) || providerId.equals(userId);
    }
}
