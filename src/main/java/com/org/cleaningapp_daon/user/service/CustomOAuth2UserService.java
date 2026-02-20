package com.org.cleaningapp_daon.user.service;

import com.org.cleaningapp_daon.user.entity.User;
import com.org.cleaningapp_daon.user.entity.dto.OauthProvider;
import com.org.cleaningapp_daon.user.entity.dto.SignupType;
import com.org.cleaningapp_daon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase(); // google, naver, kakao
        String email = null;
        String name = null;

        // Provider별 파싱
        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");

        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
            if (response != null) {
                email = (String) response.get("email");
                name = (String) response.get("name");
            }

        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    name = (String) profile.get("nickname");
                }
            }
        }

        // 이메일이 반드시 필요 (PK로 사용)
        if (email == null) {
            throw new OAuth2AuthenticationException("OAuth2 로그인 실패: email 정보를 가져올 수 없습니다. provider=" + registrationId);
        }

        OauthProvider provider = OauthProvider.valueOf(registrationId.toUpperCase());

        // 기존 사용자 확인
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isEmpty()) {
            User newUser = User.builder()
                    .userId(email)
                    .email(email)
                    .name(name != null ? name : "UNKNOWN")
                    .signupType(SignupType.OAUTH)
                    .oauthProvider(provider)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(newUser);
        }
        return oAuth2User;
    }
}
