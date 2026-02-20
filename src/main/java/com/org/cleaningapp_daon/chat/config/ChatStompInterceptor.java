package com.org.cleaningapp_daon.chat.config;

import com.org.cleaningapp_daon.chat.repository.ChatRoomRepository;
import com.org.cleaningapp_daon.security.AuthPrincipal;
import com.org.cleaningapp_daon.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatStompInterceptor implements ChannelInterceptor {

    private static final String SESSION_AUTH_KEY = "WS_AUTH_PRINCIPAL";

    private final JwtService jwtService;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
        StompCommand cmd = acc.getCommand();
        if (cmd == null) return message;

        if (cmd == StompCommand.CONNECT) {
            String authHeader = firstNativeHeader(acc, HttpHeaders.AUTHORIZATION);
            String token = resolveBearer(authHeader);

            if (token == null || !jwtService.isValid(token)) {
                throw new MessagingException("Missing/Invalid JWT");
            }

            AuthPrincipal principal = jwtService.parse(token);
            if (!"access".equals(principal.typ())) {
                throw new MessagingException("Access token required");
            }

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()));
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            //CONNECT 프레임에 user 세팅
            acc.setUser(authentication);

            //세션에 principal 저장 (SUBSCRIBE 때 여기서 꺼냄)
            Map<String, Object> sessionAttrs = acc.getSessionAttributes();
            if (sessionAttrs != null) {
                sessionAttrs.put(SESSION_AUTH_KEY, principal);
            }

            //변경 사항 반영되도록 새 Message로 리턴 (중요!)
            return MessageBuilder.createMessage(message.getPayload(), acc.getMessageHeaders());
        }

        if (cmd == StompCommand.SUBSCRIBE) {
            String dest = acc.getDestination(); // /topic/chat/{roomId}
            String userId = extractUserId(acc);

            Long roomId = parseRoomId(dest);
            if (roomId != null) {
                var room = chatRoomRepository.findById(roomId)
                        .orElseThrow(() -> new MessagingException("ChatRoom not found"));
                if (!room.isParticipant(userId)) {
                    throw new MessagingException("Forbidden subscribe");
                }
            }
            return message;
        }

        return message;
    }

    private String extractUserId(StompHeaderAccessor acc) {
        // 우선 acc.getUser()에서 시도
        Principal user = acc.getUser();
        if (user instanceof Authentication auth) {
            Object p = auth.getPrincipal();
            if (p instanceof AuthPrincipal ap) return ap.userId();
        }
        //안 되면 세션에서 꺼내기
        Map<String, Object> sessionAttrs = acc.getSessionAttributes();
        if (sessionAttrs != null) {
            Object saved = sessionAttrs.get(SESSION_AUTH_KEY);
            if (saved instanceof AuthPrincipal ap) return ap.userId();
        }
        throw new MessagingException("Unauthenticated websocket session");
    }

    private String firstNativeHeader(StompHeaderAccessor acc, String key) {
        List<String> vals = acc.getNativeHeader(key);
        return (vals == null || vals.isEmpty()) ? null : vals.get(0);
    }

    private String resolveBearer(String header) {
        if (header == null || header.isBlank()) return null;
        header = header.trim();
        if (header.startsWith("Bearer ")) return header.substring(7).trim();
        // 혹시 "Bearer<space>" 없이 들어오는 경우 대비
        if (header.startsWith("Bearer")) return header.substring(6).trim();
        return header;
    }

    private Long parseRoomId(String dest) {
        if (dest == null) return null;
        String prefix = "/topic/chat/";
        if (!dest.startsWith(prefix)) return null;
        try {
            return Long.parseLong(dest.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}