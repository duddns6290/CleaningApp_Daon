package com.org.cleaningapp_daon.video.repository ;

import com.org.cleaningapp_daon.video.entity.VideoRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRoomRepository extends JpaRepository<VideoRoom, Long> {
    Optional<VideoRoom> findByChatRoomId(Long chatRoomId);
}