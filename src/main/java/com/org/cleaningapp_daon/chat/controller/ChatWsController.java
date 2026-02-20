package com.org.cleaningapp_daon.chat.controller;

import com.org.cleaningapp_daon.chat.dto.ChatMessageResponse;
import com.org.cleaningapp_daon.chat.dto.ChatSendRequest;
import com.org.cleaningapp_daon.chat.service.ChatService;
import com.org.cleaningapp_daon.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private static final String SESSION_AUTH_KEY = "WS_AUTH_PRINCIPAL";

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/send")
    public void send(ChatSendRequest req,
                     @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {

        AuthPrincipal ap = (AuthPrincipal) sessionAttributes.get(SESSION_AUTH_KEY);
        if (ap == null) {
            throw new IllegalStateException("No authenticated WS user (session)");
        }

        ChatMessageResponse saved = chatService.sendMessage(req, ap.userId(), ap.role());
        messagingTemplate.convertAndSend("/topic/chat/" + req.roomId(), saved);
    }
}