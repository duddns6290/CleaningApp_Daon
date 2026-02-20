package com.org.cleaningapp_daon.chat.controller;

import com.org.cleaningapp_daon.chat.dto.ChatMessageResponse;
import com.org.cleaningapp_daon.chat.entity.ChatRoom;
import com.org.cleaningapp_daon.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/rooms")
    public List<ChatRoom> myRooms() {
        return chatService.myRooms();
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatMessageResponse> messages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return chatService.getMessages(roomId, page, size);
    }

    @PostMapping("/rooms/{roomId}/read")
    public void markRead(@PathVariable Long roomId) {
        chatService.markRead(roomId);
    }

    @GetMapping("/rooms/{roomId}/unread-count")
    public long unreadCount(@PathVariable Long roomId) {
        return chatService.unreadCount(roomId);
    }
}