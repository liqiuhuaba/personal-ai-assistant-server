package com.personalai.assistant.chat.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(Long sessionId, String mode, @NotBlank String message) {}
