package com.org.cleaningapp_daon.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChatRoomRequest(
        @NotNull Long requestId,
        @NotBlank String otherUserId
) {}
