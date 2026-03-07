package com.org.cleaningapp_daon.video.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SignalMessage {

    public enum Type { JOIN, LEAVE, OFFER, ANSWER, ICE, END }

    private Long sessionId;
    private Type type;

    // 누가 보냈는지
    private String fromUserId;

    // SDP/ICE payload (JSON string or plain)
    private String payload;
}