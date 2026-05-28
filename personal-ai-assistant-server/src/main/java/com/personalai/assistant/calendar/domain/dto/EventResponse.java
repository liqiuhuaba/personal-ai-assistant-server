package com.personalai.assistant.calendar.domain.dto;

import java.time.LocalDateTime;

public record EventResponse(
    Long id, String title,
    LocalDateTime startTime, LocalDateTime endTime,
    LocalDateTime remindAt, String source
) {}
