package com.personalai.assistant.chat;

import com.personalai.assistant.chat.domain.ChatMessage;
import com.personalai.assistant.chat.domain.ChatSession;
import com.personalai.assistant.chat.domain.dto.ChatRequest;
import com.personalai.assistant.chat.domain.dto.ChatResponse;
import com.personalai.assistant.chat.domain.dto.SessionResponse;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final OpenAiService openAiService;

    @Value("${openai.model}")
    private String model;

    public ChatResponse chat(Long userId, ChatRequest req) {
        Long sessionId = req.sessionId();
        if (sessionId != null) {
            ChatSession existing = sessionMapper.findById(sessionId);
            if (existing == null || !existing.getUserId().equals(userId)) {
                throw new com.personalai.assistant.common.BizException("Session not found");
            }
        }
        if (sessionId == null) {
            ChatSession session = new ChatSession();
            session.setUserId(userId);
            session.setMode(req.mode() != null ? req.mode() : "chat");
            session.setTitle(req.message().substring(0, Math.min(req.message().length(), 30)));
            sessionMapper.insert(session);
            sessionId = session.getId();
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(req.message());
        messageMapper.insert(userMsg);

        List<ChatMessage> history = messageMapper.findBySessionId(sessionId);
        List<com.theokanning.openai.completion.chat.ChatMessage> openAiMessages = history.stream()
            .map(m -> new com.theokanning.openai.completion.chat.ChatMessage(m.getRole(), m.getContent()))
            .collect(Collectors.toCollection(ArrayList::new));

        var completionReq = ChatCompletionRequest.builder()
            .model(model)
            .messages(openAiMessages)
            .build();

        var choices = openAiService.createChatCompletion(completionReq).getChoices();
        if (choices == null || choices.isEmpty()) {
            throw new com.personalai.assistant.common.BizException("AI service returned no response");
        }
        String reply = choices.get(0).getMessage().getContent();

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        messageMapper.insert(assistantMsg);

        return new ChatResponse(sessionId, reply);
    }

    public List<SessionResponse> listSessions(Long userId) {
        return sessionMapper.findByUserId(userId).stream()
            .map(s -> new SessionResponse(s.getId(), s.getMode(), s.getTitle(), s.getUpdatedAt()))
            .toList();
    }
}
