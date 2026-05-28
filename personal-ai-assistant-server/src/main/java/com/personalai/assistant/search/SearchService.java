package com.personalai.assistant.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalai.assistant.common.BizException;
import com.personalai.assistant.search.domain.SearchHistory;
import com.personalai.assistant.search.domain.dto.SearchRequest;
import com.personalai.assistant.search.domain.dto.SearchResponse;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final TavilyClient tavilyClient;
    private final SearchHistoryMapper searchHistoryMapper;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    public SearchResponse search(Long userId, SearchRequest req) {
        List<TavilyClient.TavilyResult> results = tavilyClient.search(req.query());

        String context = results.stream()
            .map(r -> String.format("标题：%s\n内容：%s\n来源：%s", r.title(), r.content(), r.url()))
            .collect(Collectors.joining("\n\n"));

        String prompt = String.format("""
            用户问题：%s

            以下是搜索到的相关内容：
            %s

            请用简洁清晰的中文汇总回答用户问题，并在末尾标注信息来源序号。
            """, req.query(), context);

        var chatReq = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(new com.theokanning.openai.completion.chat.ChatMessage("user", prompt)))
            .build();

        var choices = openAiService.createChatCompletion(chatReq).getChoices();
        if (choices == null || choices.isEmpty()) {
            throw new BizException("AI service returned no response");
        }
        String summary = choices.get(0).getMessage().getContent();

        List<SearchResponse.SourceItem> sources = results.stream()
            .map(r -> new SearchResponse.SourceItem(r.title(), r.url()))
            .toList();

        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setQuery(req.query());
        history.setSummary(summary);
        try {
            history.setSources(objectMapper.writeValueAsString(sources));
        } catch (Exception e) {
            log.warn("Failed to serialize sources", e);
        }
        searchHistoryMapper.insert(history);

        return new SearchResponse(summary, sources, history.getId());
    }

    public List<SearchHistory> listHistory(Long userId) {
        return searchHistoryMapper.findByUserId(userId);
    }

    public void toggleStar(Long userId, Long historyId, boolean starred) {
        searchHistoryMapper.updateStarred(historyId, userId, starred);
    }
}
