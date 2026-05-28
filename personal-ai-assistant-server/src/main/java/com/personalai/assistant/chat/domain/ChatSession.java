package com.personalai.assistant.chat.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSession {
    private Long id;
    private Long userId;
    private String mode;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
