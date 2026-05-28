package com.personalai.assistant.news;

import com.personalai.assistant.search.TavilyClient;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final TavilyClient tavilyClient;
    private final OpenAiService openAiService;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY = "news:today";

    @Value("${openai.model}")
    private String model;

    public void refreshDigest() {
        log.info("Refreshing news digest...");
        List<TavilyClient.TavilyResult> results = tavilyClient.search("今日重点新闻 科技 财经 社会");

        String context = results.stream()
            .map(r -> String.format("标题：%s\n摘要：%s", r.title(), r.content()))
            .collect(Collectors.joining("\n\n"));

        String prompt = String.format("""
            请从以下搜索结果中提炼今日重点新闻，以简洁的要点形式呈现（3-5条），每条不超过50字：

            %s
            """, context);

        var req = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(new com.theokanning.openai.completion.chat.ChatMessage("user", prompt)))
            .build();

        var choices = openAiService.createChatCompletion(req).getChoices();
        if (choices == null || choices.isEmpty()) {
            log.warn("AI service returned no choices for news digest, skipping cache update.");
            return;
        }
        String digest = choices.get(0).getMessage().getContent();

        redisTemplate.opsForValue().set(REDIS_KEY, digest, Duration.ofHours(25));
        log.info("News digest refreshed and cached.");
    }

    public String getTodayDigest() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        return cached != null ? cached : "今日新闻正在加载中，请稍后刷新...";
    }
}
