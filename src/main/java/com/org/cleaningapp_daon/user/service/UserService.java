package com.org.cleaningapp_daon.user.service;

import com.org.cleaningapp_daon.user.entity.User;
import com.org.cleaningapp_daon.user.entity.dto.SignupRequest;
import com.org.cleaningapp_daon.user.entity.dto.SignupResponse;
import com.org.cleaningapp_daon.user.entity.dto.SignupType;
import com.org.cleaningapp_daon.user.entity.dto.UserRole;
import com.org.cleaningapp_daon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 휴대폰 번호 중복 확인 (휴대폰 번호가 제공된 경우)
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            userRepository.findByPhone(request.getPhone())
                    .ifPresent(user -> {
                        throw new IllegalArgumentException("이미 사용 중인 휴대폰 번호입니다.");
                    });
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 사용자 생성
        User newUser = User.builder()
                .userId(request.getEmail()) // 이메일을 userId로 사용
                .email(request.getEmail())
                .name(request.getName())
                .password(encodedPassword)
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(UserRole.CUSTOMER) // 당분간 기본 CUSTOMER
                .signupType(SignupType.NORMAL)
                .oauthProvider(null) // 일반 회원가입은 OAuth Provider 없음
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);

        return SignupResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .message("회원가입이 완료되었습니다.")
                .build();
    }
}
