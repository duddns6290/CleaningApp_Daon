package com.org.cleaningapp_daon.chat.service;

import com.org.cleaningapp_daon.chat.dto.ChatMessageResponse;
import com.org.cleaningapp_daon.chat.dto.ChatRoomResponse;
import com.org.cleaningapp_daon.chat.dto.ChatSendRequest;
import com.org.cleaningapp_daon.chat.entity.ChatMessage;
import com.org.cleaningapp_daon.chat.entity.ChatRoom;
import com.org.cleaningapp_daon.chat.repository.ChatMessageRepository;
import com.org.cleaningapp_daon.chat.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AuthFacade auth;

    @Transactional
    public ChatRoomResponse createRoomIfAbsent(Long requestId, String customerId, String providerId) {
        ChatRoom room = chatRoomRepository.findByRequestId(requestId)
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .requestId(requestId)
                        .customerId(customerId)
                        .providerId(providerId)
                        .lastMessage(null)
                        .lastMessageAt(null)
                        .build()));

        return new ChatRoomResponse(
                room.getId(),
                room.getRequestId(),
                room.getCustomerId(),
                room.getProviderId(),
                room.getLastMessage(),
                room.getLastMessageAt()
        );
    }

    public List<ChatRoom> myRooms() {
        String me = auth.currentUserId(); //   String
        return chatRoomRepository.findByCustomerIdOrProviderIdOrderByLastMessageAtDesc(me, me);
    }

    public List<ChatMessageResponse> getMessages(Long roomId, int page, int size) {
        ChatRoom room = mustGetRoom(roomId);
        mustBeParticipant(room);

        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, PageRequest.of(page, size))
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(ChatSendRequest req) {
        ChatRoom room = mustGetRoom(req.roomId());
        mustBeParticipant(room);

        String me = auth.currentUserId(); //   String
        ChatMessage.SenderRole role = ChatMessage.SenderRole.valueOf(auth.currentRole());

        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .roomId(room.getId())
                .senderId(me) //   String
                .senderRole(role)
                .message(req.message())
                .readYn(false)
                .build());

        room.setLastMessage(req.message());
        room.setLastMessageAt(Instant.now());
        chatRoomRepository.save(room);

        return ChatMessageResponse.from(saved);
    }

    @Transactional
    public void markRead(Long roomId) {
        ChatRoom room = mustGetRoom(roomId);
        mustBeParticipant(room);

        String me = auth.currentUserId(); //   String
        var page = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, PageRequest.of(0, 200));

        page.forEach(m -> {
            if (!m.getSenderId().equals(me) && !m.isReadYn()) {
                m.setReadYn(true);
            }
        });

        chatMessageRepository.saveAll(page);
    }

    public long unreadCount(Long roomId) {
        ChatRoom room = mustGetRoom(roomId);
        mustBeParticipant(room);

        String me = auth.currentUserId(); //   String
        return chatMessageRepository.countByRoomIdAndReadYnFalseAndSenderIdNot(roomId, me);
    }

    private ChatRoom mustGetRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + roomId));
    }

    private void mustBeParticipant(ChatRoom room) {
        String me = auth.currentUserId(); //   String
        if (!room.isParticipant(me)) {
            throw new SecurityException("Not a participant of room: " + room.getId());
        }
    }

    @Transactional
    public ChatMessageResponse sendMessage(ChatSendRequest req, String userId, String roleStr) {
        ChatRoom room = mustGetRoom(req.roomId());
        // 참여자 검증
        if (!room.isParticipant(userId)) {
            throw new SecurityException("Not a participant of room: " + room.getId());
        }

        ChatMessage.SenderRole role = ChatMessage.SenderRole.valueOf(roleStr);

        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .roomId(room.getId())
                .senderId(userId)
                .senderRole(role)
                .message(req.message())
                .readYn(false)
                .build());

        room.setLastMessage(req.message());
        room.setLastMessageAt(Instant.now());
        chatRoomRepository.save(room);

        return ChatMessageResponse.from(saved);
    }

}