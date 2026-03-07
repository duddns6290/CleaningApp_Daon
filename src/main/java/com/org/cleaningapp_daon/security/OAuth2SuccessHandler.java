package com.org.cleaningapp_daon.security;

import com.org.cleaningapp_daon.user.entity.User;
import com.org.cleaningapp_daon.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = token.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            // naver/kakao는 구조가 다름 → DB에서 이미 저장했으니 email 기반 조회
            email = extractEmailFromAttributes(oAuth2User);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after OAuth login"));

        // JWT 발급
        String accessToken = jwtService.createAccessToken(user.getUserId(), "CUSTOMER");
        String refreshToken = jwtService.createRefreshToken(user.getUserId(), "CUSTOMER");

        // refresh는 HttpOnly 쿠키
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(14 * 24 * 60 * 60);
        response.addCookie(cookie);

        // access는 fragment로 전달
        response.sendRedirect(redirectUri + "#accessToken=" + accessToken);
    }

    private String extractEmailFromAttributes(OAuth2User user) {
        if (user.getAttribute("response") != null) {
            var response = (java.util.Map<String, Object>) user.getAttribute("response");
            return (String) response.get("email");
        }
        if (user.getAttribute("kakao_account") != null) {
            var account = (java.util.Map<String, Object>) user.getAttribute("kakao_account");
            return (String) account.get("email");
        }
        return null;
    }
}