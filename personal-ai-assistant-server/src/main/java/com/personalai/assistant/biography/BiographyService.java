package com.personalai.assistant.biography;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalai.assistant.biography.domain.BiographyEvent;
import com.personalai.assistant.biography.domain.dto.BiographyChatResponse;
import com.personalai.assistant.biography.domain.dto.BiographyEventResponse;
import com.personalai.assistant.biography.domain.dto.GenerateBiographyResponse;
import com.personalai.assistant.chat.ChatMessageMapper;
import com.personalai.assistant.chat.ChatSessionMapper;
import com.personalai.assistant.chat.domain.ChatMessage;
import com.personalai.assistant.chat.domain.ChatSession;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiographyService {

    private final BiographyEventMapper biographyEventMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
        你是一个专门帮用户记录人生故事的AI助手。在陪用户聊过往经历时，你有两个任务：
        1. 给出温暖、有引导性的回复，鼓励用户继续讲述。
        2. 从用户的叙述中提取关键人生事件。

        你必须严格以JSON格式回复，格式如下：
        {"reply":"你的回复内容","events":[{"event_date":"YYYY或YYYY-MM或YYYY-MM-DD","title":"事件标题","content":"详细描述","category":"童年/学业/职业/家庭/其他"}]}
        如果没有可提取的事件，events数组为空。只输出JSON，不要有其他内容。
        """;

    public BiographyChatResponse chat(Long userId, Long sessionId, String message) {
        if (sessionId != null) {
            ChatSession existing = sessionMapper.findById(sessionId);
            if (existing == null || !existing.getUserId().equals(userId)) {
                throw new com.personalai.assistant.common.BizException("Session not found");
            }
        }

        if (sessionId == null) {
            ChatSession session = new ChatSession();
            session.setUserId(userId);
            session.setMode("biography");
            session.setTitle(message.substring(0, Math.min(message.length(), 30)));
            sessionMapper.insert(session);
            sessionId = session.getId();
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        messageMapper.insert(userMsg);

        List<ChatMessage> history = messageMapper.findBySessionId(sessionId);
        List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();
        messages.add(new com.theokanning.openai.completion.chat.ChatMessage("system", SYSTEM_PROMPT));
        history.forEach(m -> messages.add(
            new com.theokanning.openai.completion.chat.ChatMessage(m.getRole(), m.getContent())));

        var req = ChatCompletionRequest.builder().model(model).messages(messages).build();
        var choices = openAiService.createChatCompletion(req).getChoices();
        if (choices == null || choices.isEmpty()) {
            throw new com.personalai.assistant.common.BizException("AI service returned no response");
        }
        String rawReply = choices.get(0).getMessage().getContent();

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(rawReply);
        messageMapper.insert(assistantMsg);

        String replyText = rawReply;
        List<BiographyEventResponse> extractedEvents = List.of();

        try {
            JsonNode node = objectMapper.readTree(rawReply);
            replyText = node.path("reply").asText(rawReply);
            List<BiographyEvent> events = new ArrayList<>();
            JsonNode eventsNode = node.path("events");
            if (eventsNode.isArray()) {
                for (JsonNode ev : eventsNode) {
                    String eventDate = ev.path("event_date").asText("");
                    String title = ev.path("title").asText("");
                    String content = ev.path("content").asText("");
                    String category = ev.path("category").asText("");
                    if (eventDate.isEmpty() || title.isEmpty()) continue;
                    BiographyEvent event = new BiographyEvent();
                    event.setUserId(userId);
                    event.setEventDate(eventDate);
                    event.setTitle(title);
                    event.setContent(content);
                    event.setCategory(category);
                    event.setSourceMsgId(assistantMsg.getId());
                    biographyEventMapper.insert(event);
                    events.add(event);
                }
            }
            extractedEvents = events.stream()
                .map(e -> new BiographyEventResponse(e.getId(), e.getEventDate(),
                    e.getTitle(), e.getContent(), e.getCategory()))
                .toList();
        } catch (Exception e) {
            log.warn("Failed to parse biography GPT response as JSON, using raw reply: {}", e.getMessage());
        }

        return new BiographyChatResponse(sessionId, replyText, extractedEvents);
    }

    public List<BiographyEventResponse> listEvents(Long userId) {
        return biographyEventMapper.findByUserId(userId).stream()
            .map(e -> new BiographyEventResponse(e.getId(), e.getEventDate(),
                e.getTitle(), e.getContent(), e.getCategory()))
            .toList();
    }

    public GenerateBiographyResponse generateBiography(Long userId) {
        List<BiographyEvent> events = biographyEventMapper.findByUserId(userId);
        if (events.isEmpty()) {
            return new GenerateBiographyResponse("# 我的传记\n\n还没有记录任何人生事件，请先在传记模式中聊聊你的过往经历。");
        }

        String eventsText = events.stream()
            .map(e -> String.format("- [%s] %s: %s", e.getEventDate(), e.getTitle(), e.getContent()))
            .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            请根据以下人生事件，以第一人称撰写一篇完整的个人传记草稿，Markdown格式，语言温暖真实：

            %s
            """, eventsText);

        var genReq = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(new com.theokanning.openai.completion.chat.ChatMessage("user", prompt)))
            .build();

        var genChoices = openAiService.createChatCompletion(genReq).getChoices();
        if (genChoices == null || genChoices.isEmpty()) {
            throw new com.personalai.assistant.common.BizException("AI service returned no response");
        }
        String markdown = genChoices.get(0).getMessage().getContent();
        return new GenerateBiographyResponse(markdown);
    }
}
