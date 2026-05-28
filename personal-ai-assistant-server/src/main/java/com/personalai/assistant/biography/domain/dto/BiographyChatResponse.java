package com.personalai.assistant.biography.domain.dto;
import java.util.List;
public record BiographyChatResponse(Long sessionId, String reply, List<BiographyEventResponse> extractedEvents) {}
