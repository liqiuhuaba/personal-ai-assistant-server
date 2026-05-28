package com.personalai.assistant.calendar.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalendarEvent {
    private Long id;
    private Long userId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime remindAt;
    private String source;
    private LocalDateTime createdAt;
}
