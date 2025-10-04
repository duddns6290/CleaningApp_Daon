package com.org.cleaningapp_daon.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

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
