package com.org.cleaningapp_daon.video.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateSessionRequest {
    private Long chatRoomId;
}