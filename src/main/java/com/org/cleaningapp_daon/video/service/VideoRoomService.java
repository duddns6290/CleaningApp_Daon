package com.org.cleaningapp_daon.video.service;

import com.org.cleaningapp_daon.chat.entity.ChatRoom;
import com.org.cleaningapp_daon.chat.repository.ChatRoomRepository;
import com.org.cleaningapp_daon.video.entity.VideoRoom;
import com.org.cleaningapp_daon.video.repository.VideoRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoRoomService {

    private final VideoRoomRepository videoRoomRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public VideoRoom getOrCreateByChatRoomId(Long chatRoomId) {
        return videoRoomRepository.findByChatRoomId(chatRoomId)
                .orElseGet(() -> {
                    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                            .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + chatRoomId));

                    VideoRoom vr = VideoRoom.builder()
                            .chatRoomId(chatRoom.getId())
                            .customerId(chatRoom.getCustomerId())
                            .providerId(chatRoom.getProviderId())
                            .build();

                    return videoRoomRepository.save(vr);
                });
    }
}