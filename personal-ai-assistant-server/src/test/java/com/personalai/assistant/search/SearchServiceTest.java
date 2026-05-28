package com.personalai.assistant.search;

import com.personalai.assistant.search.domain.dto.SearchRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock TavilyClient tavilyClient;
    @Mock SearchHistoryMapper searchHistoryMapper;
    @Mock OpenAiService openAiService;
    @Spy com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    @InjectMocks SearchService searchService;

    @Test
    void search_callsTavilyThenGptAndSavesHistory() {
        when(tavilyClient.search("GPT-5发布")).thenReturn(List.of(
            new TavilyClient.TavilyResult("GPT-5已发布", "https://openai.com", "OpenAI宣布发布GPT-5...")
        ));

        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", "GPT-5已于近日正式发布。");
        var choice = new ChatCompletionChoice(); choice.setMessage(mockMsg);
        var result = new ChatCompletionResult(); result.setChoices(List.of(choice));
        when(openAiService.createChatCompletion(any())).thenReturn(result);
        doNothing().when(searchHistoryMapper).insert(any());

        var response = searchService.search(1L, new SearchRequest("GPT-5发布", null));

        assertThat(response.summary()).isEqualTo("GPT-5已于近日正式发布。");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).url()).isEqualTo("https://openai.com");
        verify(searchHistoryMapper).insert(any());
    }
}
