package com.personalai.assistant.learning;

import com.personalai.assistant.chat.ChatMessageMapper;
import com.personalai.assistant.chat.ChatSessionMapper;
import com.personalai.assistant.chat.OpenAiClient;
import com.personalai.assistant.chat.domain.ChatMessage;
import com.personalai.assistant.chat.domain.ChatSession;
import com.personalai.assistant.common.BizException;
import com.personalai.assistant.learning.domain.LearningSession;
import com.personalai.assistant.learning.domain.dto.LearningChatRequest;
import com.personalai.assistant.learning.domain.dto.LearningChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningService {

    private final LearningSessionMapper learningSessionMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper messageMapper;
    private final OpenAiClient openAiClient;

    public LearningChatResponse chat(Long userId, LearningChatRequest req) {
        Long sessionId = req.sessionId();

        if (sessionId != null) {
            ChatSession existing = chatSessionMapper.findById(sessionId);
            if (existing == null || !existing.getUserId().equals(userId)) {
                throw new BizException("Session not found");
            }
        }

        if (sessionId == null) {
            ChatSession chatSession = new ChatSession();
            chatSession.setUserId(userId);
            chatSession.setMode("learning");
            chatSession.setTitle(req.subject() + " · " + (req.topic() != null ? req.topic() : ""));
            chatSessionMapper.insert(chatSession);
            sessionId = chatSession.getId();

            LearningSession ls = new LearningSession();
            ls.setUserId(userId);
            ls.setSubject(req.subject());
            ls.setTopic(req.topic());
            learningSessionMapper.insert(ls);
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(req.message());
        messageMapper.insert(userMsg);

        String systemPrompt = String.format("""
            你是一个专业的学习陪练AI，专注于 %s 科目%s。
            你的职责是：出题考察、耐心讲解、追踪理解程度，用鼓励的语气帮助用户学习。
            对用户的回答给出正误判断和详细解析。
            """, req.subject(), req.topic() != null ? "中的 " + req.topic() + " 主题" : "");

        List<ChatMessage> history = messageMapper.findBySessionId(sessionId);
        List<OpenAiClient.Message> messages = new ArrayList<>();
        messages.add(new OpenAiClient.Message("system", systemPrompt));
        history.forEach(m -> messages.add(new OpenAiClient.Message(m.getRole(), m.getContent())));

        String reply = openAiClient.chat(messages);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        messageMapper.insert(assistantMsg);

        return new LearningChatResponse(sessionId, reply);
    }

    public List<LearningSession> listSessions(Long userId) {
        return learningSessionMapper.findByUserId(userId);
    }
}
