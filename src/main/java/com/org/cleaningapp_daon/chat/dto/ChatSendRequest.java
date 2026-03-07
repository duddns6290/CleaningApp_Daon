package com.org.cleaningapp_daon.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatSendRequest(
        @NotNull Long roomId,
        @NotBlank String message
) {}