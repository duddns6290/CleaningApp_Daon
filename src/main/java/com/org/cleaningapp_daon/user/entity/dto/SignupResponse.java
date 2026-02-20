package com.org.cleaningapp_daon.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private String userId;
    private String email;
    private String name;
    private String message;
}
