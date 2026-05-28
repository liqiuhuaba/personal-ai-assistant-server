package com.personalai.assistant.chat;

import com.personalai.assistant.chat.domain.ChatMessage;
import com.personalai.assistant.chat.domain.ChatSession;
import com.personalai.assistant.chat.domain.dto.ChatRequest;
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
class ChatServiceTest {

    @Mock ChatSessionMapper sessionMapper;
    @Mock ChatMessageMapper messageMapper;
    @Mock OpenAiService openAiService;
    @InjectMocks ChatService chatService;

    @Test
    void chat_createsSessionAndReturnsReply() {
        doAnswer(inv -> { ((ChatSession) inv.getArgument(0)).setId(1L); return null; })
            .when(sessionMapper).insert(any());
        doNothing().when(messageMapper).insert(any());

        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", "Hello back!");
        var mockChoice = new ChatCompletionChoice();
        mockChoice.setMessage(mockMsg);
        var mockResult = new ChatCompletionResult();
        mockResult.setChoices(List.of(mockChoice));
        when(openAiService.createChatCompletion(any())).thenReturn(mockResult);
        when(messageMapper.findBySessionId(1L)).thenReturn(List.of());

        var req = new ChatRequest(null, "chat", "Hello AI");
        var response = chatService.chat(1L, req);

        assertThat(response.reply()).isEqualTo("Hello back!");
        assertThat(response.sessionId()).isEqualTo(1L);
        verify(messageMapper, times(2)).insert(any()); // user msg + assistant msg
    }
}
