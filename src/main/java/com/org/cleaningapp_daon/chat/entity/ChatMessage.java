package com.org.cleaningapp_daon.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "chat_message",
        indexes = {
                @Index(name="idx_msg_room_created", columnList="roomId, createdAt")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private String senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SenderRole senderRole;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private boolean readYn;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        this.readYn = false;
    }

    public enum SenderRole {
        CUSTOMER, PROVIDER
    }
}
