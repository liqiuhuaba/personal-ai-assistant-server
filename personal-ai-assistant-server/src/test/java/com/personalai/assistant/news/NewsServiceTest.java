package com.personalai.assistant.news;

import com.personalai.assistant.search.TavilyClient;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock TavilyClient tavilyClient;
    @Mock OpenAiService openAiService;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @InjectMocks NewsService newsService;

    @Test
    void refresh_fetchesAndCachesDigest() {
        when(tavilyClient.search(anyString())).thenReturn(List.of(
            new TavilyClient.TavilyResult("AI新闻", "https://example.com", "今日AI大事...")
        ));
        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", "今日要闻：AI领域重大进展...");
        var choice = new ChatCompletionChoice(); choice.setMessage(mockMsg);
        var result = new ChatCompletionResult(); result.setChoices(List.of(choice));
        when(openAiService.createChatCompletion(any())).thenReturn(result);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        doNothing().when(valueOps).set(anyString(), anyString(), any());

        newsService.refreshDigest();

        verify(valueOps).set(eq("news:today"), anyString(), any(Duration.class));
    }

    @Test
    void getTodayDigest_returnsCachedValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("news:today")).thenReturn("今日要闻：...");

        String digest = newsService.getTodayDigest();

        assertThat(digest).isEqualTo("今日要闻：...");
    }
}
