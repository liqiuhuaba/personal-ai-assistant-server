package com.personalai.assistant.user.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String avatarUrl;
    private Boolean cloudSync;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
