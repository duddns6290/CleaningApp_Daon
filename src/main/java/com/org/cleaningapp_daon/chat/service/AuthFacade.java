package com.org.cleaningapp_daon.chat.service;

import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    //TODO: JWT 연동 시 SecurityContext에서 userId/role 꺼내도록 수정
    public Long currentUserId() {
        // 임시 테스트값
        return 1L;
    }

    public String currentRole() {
        // "CUSTOMER" or "PROVIDER"
        return "CUSTOMER";
    }
}