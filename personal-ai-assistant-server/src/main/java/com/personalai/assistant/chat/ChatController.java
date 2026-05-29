package com.personalai.assistant.chat;

import com.personalai.assistant.chat.domain.dto.ChatRequest;
import com.personalai.assistant.chat.domain.dto.ChatResponse;
import com.personalai.assistant.chat.domain.dto.MessageResponse;
import com.personalai.assistant.chat.domain.dto.SessionResponse;
import com.personalai.assistant.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ApiResponse<ChatResponse> chat(Authentication auth,
                                          @Valid @RequestBody ChatRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(chatService.chat(userId, req));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionResponse>> sessions(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(chatService.listSessions(userId));
    }

    @GetMapping("/sessions/{id}/messages")
    public ApiResponse<List<MessageResponse>> sessionMessages(Authentication auth,
                                                             @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(chatService.getSessionMessages(userId, id));
    }
}
