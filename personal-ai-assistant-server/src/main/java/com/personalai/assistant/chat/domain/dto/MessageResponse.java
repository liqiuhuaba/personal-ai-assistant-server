package com.personalai.assistant.chat.domain.dto;

import java.time.LocalDateTime;

public record MessageResponse(
    Long id,
    String role,
    String content,
    LocalDateTime createdAt
) {}
