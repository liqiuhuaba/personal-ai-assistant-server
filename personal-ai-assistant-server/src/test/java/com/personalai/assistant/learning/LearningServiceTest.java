package com.personalai.assistant.learning;

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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningServiceTest {

    @Mock LearningSessionMapper learningSessionMapper;
    @Mock ChatSessionMapper chatSessionMapper;
    @Mock ChatMessageMapper messageMapper;
    @Mock OpenAiService openAiService;
    @InjectMocks LearningService learningService;

    @Test
    void chat_returnsAiTutorReply() {
        doAnswer(inv -> { var s = (com.personalai.assistant.chat.domain.ChatSession) inv.getArgument(0); s.setId(1L); return null; })
            .when(chatSessionMapper).insert(any());
        doNothing().when(messageMapper).insert(any());
        when(messageMapper.findBySessionId(1L)).thenReturn(List.of());

        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", "Great question! The answer is 42.");
        var choice = new ChatCompletionChoice(); choice.setMessage(mockMsg);
        var result = new ChatCompletionResult(); result.setChoices(List.of(choice));
        when(openAiService.createChatCompletion(any())).thenReturn(result);

        var req = new com.personalai.assistant.learning.domain.dto.LearningChatRequest(null, "数学", "高中代数", "什么是二次方程？");
        var response = learningService.chat(1L, req);

        assertThat(response.reply()).isEqualTo("Great question! The answer is 42.");
        assertThat(response.sessionId()).isEqualTo(1L);
    }
}
