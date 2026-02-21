package com.org.cleaningapp_daon.video.service;

import com.org.cleaningapp_daon.video.entity.VideoRoom;
import com.org.cleaningapp_daon.video.entity.VideoSession;
import com.org.cleaningapp_daon.video.repository.VideoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoSessionService {

    private final VideoRoomService videoRoomService;
    private final VideoSessionRepository videoSessionRepository;

    @Transactional
    public VideoSession createOrReuseRingingSession(Long chatRoomId, String userId) {
        VideoRoom room = videoRoomService.getOrCreateByChatRoomId(chatRoomId);

        // participant 검증 (이메일 기준)
        if (!room.isParticipant(userId)) {
            throw new SecurityException("Not a participant of chatRoomId=" + chatRoomId);
        }

        // 이미 RINGING/ACTIVE 세션 있으면 재사용 (중복 클릭 방지)
        return videoSessionRepository
                .findFirstByVideoRoomIdAndStatusInOrderByCreatedAtDesc(
                        room.getId(),
                        List.of(VideoSession.Status.RINGING, VideoSession.Status.ACTIVE)
                )
                .orElseGet(() -> videoSessionRepository.save(
                        VideoSession.builder()
                                .videoRoomId(room.getId())
                                .status(VideoSession.Status.RINGING)
                                .createdBy(userId)
                                .ringingAt(Instant.now())
                                .build()
                ));
    }

    @Transactional
    public VideoSession accept(Long sessionId, String userId, Long chatRoomId) {
        VideoRoom room = videoRoomService.getOrCreateByChatRoomId(chatRoomId);
        if (!room.isParticipant(userId)) throw new SecurityException("Not a participant");

        VideoSession s = videoSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("VideoSession not found: " + sessionId));

        if (!s.getVideoRoomId().equals(room.getId())) {
            throw new IllegalArgumentException("Session-room mismatch");
        }

        // RINGING만 ACCEPT 가능
        if (s.getStatus() != VideoSession.Status.RINGING) {
            return s;
        }

        s.setStatus(VideoSession.Status.ACTIVE);
        s.setAcceptedAt(Instant.now());
        return s;
    }

    @Transactional
    public VideoSession reject(Long sessionId, String userId, Long chatRoomId) {
        VideoRoom room = videoRoomService.getOrCreateByChatRoomId(chatRoomId);
        if (!room.isParticipant(userId)) throw new SecurityException("Not a participant");

        VideoSession s = videoSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("VideoSession not found: " + sessionId));

        if (!s.getVideoRoomId().equals(room.getId())) {
            throw new IllegalArgumentException("Session-room mismatch");
        }

        if (s.getStatus() != VideoSession.Status.RINGING) {
            return s;
        }

        s.setStatus(VideoSession.Status.REJECTED);
        s.setEndedAt(Instant.now());
        return s;
    }

    @Transactional
    public VideoSession end(Long sessionId, String userId, Long chatRoomId) {
        VideoRoom room = videoRoomService.getOrCreateByChatRoomId(chatRoomId);
        if (!room.isParticipant(userId)) throw new SecurityException("Not a participant");

        VideoSession s = videoSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("VideoSession not found: " + sessionId));

        if (!s.getVideoRoomId().equals(room.getId())) {
            throw new IllegalArgumentException("Session-room mismatch");
        }

        if (s.getStatus() == VideoSession.Status.ENDED) {
            return s;
        }

        s.setStatus(VideoSession.Status.ENDED);
        s.setEndedAt(Instant.now());
        return s;
    }
}