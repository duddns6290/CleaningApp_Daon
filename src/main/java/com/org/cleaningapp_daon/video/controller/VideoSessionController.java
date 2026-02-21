package com.org.cleaningapp_daon.video.controller;

import com.org.cleaningapp_daon.security.AuthPrincipal;
import com.org.cleaningapp_daon.video.dto.CreateSessionRequest;
import com.org.cleaningapp_daon.video.dto.VideoSessionResponse;
import com.org.cleaningapp_daon.video.entity.VideoSession;
import com.org.cleaningapp_daon.video.service.VideoSessionService;
import com.org.cleaningapp_daon.video.service.VideoSignalingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/video")
public class VideoSessionController {

    private final VideoSessionService videoSessionService;

    @PostMapping("/sessions")
    public VideoSessionResponse createSession(@RequestBody CreateSessionRequest req,
                                              Authentication authentication) {

        String userId = extractUserId(authentication); // 항상 이메일
        VideoSession session = videoSessionService.createOrReuseRingingSession(req.getChatRoomId(), userId);

        return VideoSessionResponse.of(req.getChatRoomId(), session);
    }

    @PostMapping("/sessions/{id}/accept")
    public VideoSessionResponse accept(@PathVariable("id") Long sessionId,
                                       @RequestParam Long chatRoomId,
                                       Authentication authentication) {

        String userId = extractUserId(authentication);
        VideoSession s = videoSessionService.accept(sessionId, userId, chatRoomId);
        return VideoSessionResponse.of(chatRoomId, s);
    }

    @PostMapping("/sessions/{id}/reject")
    public VideoSessionResponse reject(@PathVariable("id") Long sessionId,
                                       @RequestParam Long chatRoomId,
                                       Authentication authentication) {

        String userId = extractUserId(authentication);
        VideoSession s = videoSessionService.reject(sessionId, userId, chatRoomId);
        return VideoSessionResponse.of(chatRoomId, s);
    }

    @PostMapping("/sessions/{id}/end")
    public VideoSessionResponse end(@PathVariable("id") Long sessionId,
                                    @RequestParam Long chatRoomId,
                                    Authentication authentication) {

        String userId = extractUserId(authentication);
        VideoSession s = videoSessionService.end(sessionId, userId, chatRoomId);
        return VideoSessionResponse.of(chatRoomId, s);
    }


    private String extractUserId(Authentication authentication) {
        if (authentication == null) throw new IllegalStateException("Unauthenticated");

        Object p = authentication.getPrincipal();
        if (p instanceof AuthPrincipal ap) {
            return ap.userId();
        }

        // 혹시라도 여기로 떨어지면 security 구성/필터쪽에서 principal 세팅이 다른 상태
        throw new IllegalStateException("Unexpected principal type: " + (p == null ? "null" : p.getClass().getName()));
    }
}