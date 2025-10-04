package com.org.cleaningapp_daon.user.entity;

import com.org.cleaningapp_daon.user.entity.dto.OauthProvider;
import com.org.cleaningapp_daon.user.entity.dto.SignupType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    private String email;
    private String name;
    private String address;
    private String phone;

    @Column(name = "oauth_provider")
    @Enumerated(EnumType.STRING)
    private OauthProvider oauthProvider;

    @Column(name = "signup_type")
    @Enumerated(EnumType.STRING)
    private SignupType signupType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

