package com.personalai.assistant.biography;

import com.personalai.assistant.biography.domain.BiographyEvent;
import com.personalai.assistant.chat.ChatMessageMapper;
import com.personalai.assistant.chat.ChatSessionMapper;
import com.personalai.assistant.chat.domain.ChatMessage;
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
class BiographyServiceTest {

    @Mock BiographyEventMapper biographyEventMapper;
    @Mock ChatSessionMapper sessionMapper;
    @Mock ChatMessageMapper messageMapper;
    @Mock OpenAiService openAiService;
    @Spy com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    @InjectMocks BiographyService biographyService;

    @Test
    void chat_extractsBiographyEventsFromGptResponse() {
        doAnswer(inv -> { var s = (com.personalai.assistant.chat.domain.ChatSession) inv.getArgument(0); s.setId(1L); return null; })
            .when(sessionMapper).insert(any());
        doNothing().when(messageMapper).insert(any());
        when(messageMapper.findBySessionId(1L)).thenReturn(List.of());

        String gptReply = """
            {"reply":"那真是难忘的经历！","events":[{"event_date":"1998-09","title":"考入大学","content":"1998年9月考入北京某高校计算机系","category":"学业"}]}
            """;

        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", gptReply);
        var choice = new ChatCompletionChoice(); choice.setMessage(mockMsg);
        var result = new ChatCompletionResult(); result.setChoices(List.of(choice));
        when(openAiService.createChatCompletion(any())).thenReturn(result);
        doAnswer(inv -> { ((BiographyEvent) inv.getArgument(0)).setId(1L); return null; })
            .when(biographyEventMapper).insert(any());

        var response = biographyService.chat(1L, null, "我1998年考上大学了");

        assertThat(response.reply()).isEqualTo("那真是难忘的经历！");
        assertThat(response.extractedEvents()).hasSize(1);
        assertThat(response.extractedEvents().get(0).title()).isEqualTo("考入大学");
        verify(biographyEventMapper).insert(any());
    }
}
