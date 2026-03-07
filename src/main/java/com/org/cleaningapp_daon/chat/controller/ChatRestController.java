package com.org.cleaningapp_daon.chat.controller;

import com.org.cleaningapp_daon.chat.dto.ChatMessageResponse;
import com.org.cleaningapp_daon.chat.dto.ChatRoomDetailResponse;
import com.org.cleaningapp_daon.chat.dto.ChatRoomListItemResponse;
import com.org.cleaningapp_daon.chat.dto.ChatRoomResponse;
import com.org.cleaningapp_daon.chat.dto.CreateChatRoomRequest;
import com.org.cleaningapp_daon.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRestController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ChatRoomResponse createRoom(@Valid @RequestBody CreateChatRoomRequest request) {
        return chatService.createRoom(request);
    }

    @GetMapping("/rooms")
    public List<ChatRoomListItemResponse> myRooms() {
        return chatService.myRoomListItems();
    }

    @GetMapping("/rooms/{roomId}")
    public ChatRoomDetailResponse getRoomDetail(@PathVariable Long roomId) {
        return chatService.getRoomDetail(roomId);
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