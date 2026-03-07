package com.org.cleaningapp_daon.chat.service;

import com.org.cleaningapp_daon.security.AuthPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class AuthFacade {

    private AuthPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal ap)) {
            throw new IllegalStateException("Unauthenticated user");
        }

        return ap;
    }

    public String currentUserId() {
        AuthPrincipal ap = currentPrincipal();
        return ap.userId(); // String
    }

    public String currentRole() {
        // "CUSTOMER" or "PROVIDER"
        return "CUSTOMER";
    }
}