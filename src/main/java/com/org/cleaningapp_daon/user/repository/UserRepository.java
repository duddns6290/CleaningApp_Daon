package com.org.cleaningapp_daon.user.repository;

import com.org.cleaningapp_daon.user.entity.User;
import com.org.cleaningapp_daon.user.entity.dto.OauthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);

    Optional<User> findByOauthProviderAndEmail(OauthProvider oauthProvider, String email);
}
