package com.org.cleaningapp_daon.user.controller;

import com.org.cleaningapp_daon.user.entity.dto.SignupRequest;
import com.org.cleaningapp_daon.user.entity.dto.SignupResponse;
import com.org.cleaningapp_daon.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(
            summary = "일반 회원가입",
            description = "이메일, 비밀번호를 이용한 일반 회원가입"
    )
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(
            summary = "OAuth 테스트",
            description = "login 성공 후 email, name 반환"
    )
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("email", email != null ? email : "");
        result.put("name", name != null ? name : "");

        return ResponseEntity.ok(result);
    }

}
