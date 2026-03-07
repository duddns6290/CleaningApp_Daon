package com.org.cleaningapp_daon.video.controller;

import com.org.cleaningapp_daon.security.AuthPrincipal;
import com.org.cleaningapp_daon.video.dto.SignalMessage;
import com.org.cleaningapp_daon.video.service.VideoSignalingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class VideoSignalingController {

    private static final String SESSION_AUTH_KEY = "WS_AUTH_PRINCIPAL";

    private final VideoSignalingService signalingService;

    // 클라 publish: /app/video.signal
    @MessageMapping("/video.signal")
    public void signal(SignalMessage message,
                       @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {

        AuthPrincipal ap = (AuthPrincipal) sessionAttributes.get(SESSION_AUTH_KEY);
        if (ap == null) {
            throw new IllegalStateException("No authenticated WS user (session)");
        }

        signalingService.relaySignal(message, ap.userId()); // 무조건 이메일
    }
}