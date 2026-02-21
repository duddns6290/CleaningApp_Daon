package com.org.cleaningapp_daon.video.repository;

import com.org.cleaningapp_daon.video.entity.VideoSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface VideoSessionRepository extends JpaRepository<VideoSession, Long> {

    // 진행 중(또는 수신대기) 세션이 있으면 재사용/중복 방지
    Optional<VideoSession> findFirstByVideoRoomIdAndStatusInOrderByCreatedAtDesc(
            Long videoRoomId,
            Collection<VideoSession.Status> statuses
    );
}