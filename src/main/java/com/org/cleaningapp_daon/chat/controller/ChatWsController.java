package com.org.cleaningapp_daon.chat.controller;

import com.org.cleaningapp_daon.chat.dto.ChatMessageResponse;
import com.org.cleaningapp_daon.chat.dto.ChatSendRequest;
import com.org.cleaningapp_daon.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // client -> /app/chat/send
    @MessageMapping("/chat/send")
    public void send(@Valid ChatSendRequest req) {
        ChatMessageResponse saved = chatService.sendMessage(req);
        // server -> /topic/chat/{roomId}
        messagingTemplate.convertAndSend("/topic/chat/" + req.roomId(), saved);
    }
}