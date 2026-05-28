package com.personalai.assistant.learning.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LearningSession {
    private Long id;
    private Long userId;
    private String subject;
    private String topic;
    private Integer score;
    private Integer durationMin;
    private LocalDateTime createdAt;
}
