package com.personalai.assistant.search.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SearchHistory {
    private Long id;
    private Long userId;
    private String query;
    private String summary;
    private String sources;
    private Boolean starred;
    private LocalDateTime createdAt;
}
