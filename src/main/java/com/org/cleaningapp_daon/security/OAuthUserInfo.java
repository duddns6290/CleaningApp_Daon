package com.org.cleaningapp_daon.security;

public record OAuthUserInfo(
        String provider,   // "google" / "kakao" / "naver"
        String providerId, // 구글 sub / 카카오 id / 네이버 id (참고용)
        String email,
        String name
) {}