package com.personalai.assistant.biography.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BiographyEvent {
    private Long id;
    private Long userId;
    private String eventDate;
    private String title;
    private String content;
    private String category;
    private Long sourceMsgId;
    private LocalDateTime createdAt;
}
