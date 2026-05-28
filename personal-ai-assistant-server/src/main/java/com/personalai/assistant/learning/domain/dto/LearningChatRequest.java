package com.personalai.assistant.learning.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record LearningChatRequest(Long sessionId, @NotBlank String subject, String topic, @NotBlank String message) {}
