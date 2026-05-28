package com.personalai.assistant.search.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record SearchRequest(@NotBlank String query, Long sessionId) {}
