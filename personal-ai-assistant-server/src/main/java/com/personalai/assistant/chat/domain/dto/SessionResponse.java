package com.personalai.assistant.chat.domain.dto;

import java.time.LocalDateTime;

public record SessionResponse(Long id, String mode, String title, LocalDateTime updatedAt) {}
