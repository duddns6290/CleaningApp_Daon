package com.org.cleaningapp_daon.chat.service;

import com.org.cleaningapp_daon.chat.dto.ChatMessageResponse;
import com.org.cleaningapp_daon.chat.dto.ChatRoomDetailResponse;
import com.org.cleaningapp_daon.chat.dto.ChatRoomListItemResponse;
import com.org.cleaningapp_daon.chat.dto.ChatRoomResponse;
import com.org.cleaningapp_daon.chat.dto.ChatSendRequest;
import com.org.cleaningapp_daon.chat.dto.CreateChatRoomRequest;
import com.org.cleaningapp_daon.chat.entity.ChatMessage;
import com.org.cleaningapp_daon.chat.entity.ChatRoom;
import com.org.cleaningapp_daon.chat.repository.ChatMessageRepository;
import com.org.cleaningapp_daon.chat.repository.ChatRoomRepository;
import com.org.cleaningapp_daon.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
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

    /** 로그인 사용자 + 상대방으로 채팅방 생성 또는 기존 방 반환 */
    @Transactional
    public ChatRoomResponse createRoom(CreateChatRoomRequest req) {
        String me = auth.currentUserId();
        String role = auth.currentRole();
        String customerId;
        String providerId;
        if ("PROVIDER".equalsIgnoreCase(role)) {
            customerId = req.otherUserId();
            providerId = me;
        } else {
            customerId = me;
            providerId = req.otherUserId();
        }
        return createRoomIfAbsent(req.requestId(), customerId, providerId);
    }

    public List<ChatRoom> myRooms() {
        String me = auth.currentUserId(); //   String
        return chatRoomRepository.findByCustomerIdOrProviderIdOrderByLastMessageAtDesc(me, me);
    }

    /** 내 채팅방 목록 (상대방 회원명 포함) */
    public List<ChatRoomListItemResponse> myRoomListItems() {
        String me = auth.currentUserId();
        return chatRoomRepository.findByCustomerIdOrProviderIdOrderByLastMessageAtDesc(me, me)
                .stream()
                .map(room -> {
                    String otherUserId = me.equals(room.getCustomerId()) ? room.getProviderId() : room.getCustomerId();
                    String otherUserName = userRepository.findById(otherUserId)
                            .map(u -> u.getName() != null && !u.getName().isBlank() ? u.getName() : otherUserId)
                            .orElse(otherUserId);
                    return new ChatRoomListItemResponse(
                            room.getId(),
                            room.getRequestId(),
                            room.getCustomerId(),
                            room.getProviderId(),
                            room.getLastMessage(),
                            room.getLastMessageAt(),
                            room.getCreatedAt(),
                            otherUserId,
                            otherUserName,
                            unreadCount(room.getId())
                    );
                })
                .toList();
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

        // 상대방 화면에 읽음 반영을 위해 WebSocket으로 알림
        messagingTemplate.convertAndSend("/topic/chat/" + roomId,
                java.util.Map.of("type", "ROOM_READ", "readerId", me));
    }

    public long unreadCount(Long roomId) {
        ChatRoom room = mustGetRoom(roomId);
        mustBeParticipant(room);

        String me = auth.currentUserId(); //   String
        return chatMessageRepository.countByRoomIdAndReadYnFalseAndSenderIdNot(roomId, me);
    }

    /** 채팅방 상세 (상대방 회원명 등). 참여자만 조회 가능. */
    public ChatRoomDetailResponse getRoomDetail(Long roomId) {
        ChatRoom room = mustGetRoom(roomId);
        mustBeParticipant(room);

        String me = auth.currentUserId();
        String otherUserId = me.equals(room.getCustomerId()) ? room.getProviderId() : room.getCustomerId();
        String otherUserName = userRepository.findById(otherUserId)
                .map(u -> u.getName() != null && !u.getName().isBlank() ? u.getName() : otherUserId)
                .orElse(otherUserId);

        return new ChatRoomDetailResponse(room.getId(), otherUserId, otherUserName);
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