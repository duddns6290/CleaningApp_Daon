package com.org.cleaningapp_daon.video.service;

import com.org.cleaningapp_daon.video.dto.SignalMessage;
import com.org.cleaningapp_daon.video.entity.VideoRoom;
import com.org.cleaningapp_daon.video.entity.VideoSession;
import com.org.cleaningapp_daon.video.repository.VideoRoomRepository;
import com.org.cleaningapp_daon.video.repository.VideoSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoSignalingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final VideoSessionRepository videoSessionRepository;
    private final VideoRoomRepository videoRoomRepository;

    @Transactional(readOnly = true)
    public void relaySignal(SignalMessage msg, String userId) {
        if (msg.getSessionId() == null || msg.getType() == null) {
            throw new IllegalArgumentException("sessionId/type required");
        }

        VideoSession session = videoSessionRepository.findById(msg.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("VideoSession not found: " + msg.getSessionId()));

        VideoRoom room = videoRoomRepository.findById(session.getVideoRoomId())
                .orElseThrow(() -> new IllegalStateException("VideoRoom not found: " + session.getVideoRoomId()));

        if (!room.isParticipant(userId)) {
            System.out.println("[SIGNAL DENY] userId=" + userId
                    + " not participant. room(customer=" + room.getCustomerId()
                    + ", provider=" + room.getProviderId() + ")");
            throw new SecurityException("Not a participant");
        }

        // END는 세션 상태가 어떻든 보내도록 허용
        if (msg.getType() != SignalMessage.Type.END) {
            if (!(session.getStatus() == VideoSession.Status.RINGING || session.getStatus() == VideoSession.Status.ACTIVE)) {
                System.out.println("[SIGNAL DENY] sessionId=" + msg.getSessionId()
                        + " status=" + session.getStatus() + " type=" + msg.getType());
                throw new IllegalStateException("Session not active/ringing: " + session.getStatus());
            }
        }

        // fromUserId는 서버 값으로 덮어쓰기
        msg.setFromUserId(userId);

        String destination = "/topic/video.session." + msg.getSessionId();

        System.out.println("[SERVICE] OUT dest=/topic/video.session." + msg.getSessionId());

        System.out.println("[SIGNAL IN] from=" + userId
                + " sessionId=" + msg.getSessionId()
                + " type=" + msg.getType()
                + " -> " + destination);

        messagingTemplate.convertAndSend(destination, msg);

        System.out.println("[SIGNAL OUT] " + destination);
    }

}