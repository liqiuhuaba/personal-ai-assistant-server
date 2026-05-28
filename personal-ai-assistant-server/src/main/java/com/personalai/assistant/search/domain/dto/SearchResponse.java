package com.personalai.assistant.search.domain.dto;
import java.util.List;
public record SearchResponse(String summary, List<SourceItem> sources, Long historyId) {
    public record SourceItem(String title, String url) {}
}
