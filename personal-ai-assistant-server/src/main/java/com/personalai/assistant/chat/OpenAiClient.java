package com.personalai.assistant.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenAiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String model;

    public OpenAiClient(RestTemplate openAiRestTemplate,
                        ObjectMapper objectMapper,
                        @Value("${openai.base-url}") String baseUrl,
                        @Value("${openai.model}") String model) {
        this.restTemplate = openAiRestTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.model = model;
    }

    public String chat(List<Message> messages) {
        try {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("model", model);
            
            ArrayNode messagesArray = request.putArray("messages");
            for (Message msg : messages) {
                ObjectNode msgNode = messagesArray.addObject();
                msgNode.put("role", msg.role());
                msgNode.put("content", msg.content());
            }

            String url = baseUrl + "chat/completions";
            String response = restTemplate.postForObject(url, request, String.class);
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
            throw new RuntimeException("No response from AI");
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI API: " + e.getMessage(), e);
        }
    }

    public record Message(String role, String content) {}
}
