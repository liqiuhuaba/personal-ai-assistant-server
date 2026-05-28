package com.personalai.assistant.calendar.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateEventRequest(
    @NotBlank String title,
    @NotNull LocalDateTime startTime,
    LocalDateTime endTime,
    LocalDateTime remindAt,
    String source
) {}
