package com.personalai.assistant.learning;

import com.personalai.assistant.common.ApiResponse;
import com.personalai.assistant.learning.domain.LearningSession;
import com.personalai.assistant.learning.domain.dto.LearningChatRequest;
import com.personalai.assistant.learning.domain.dto.LearningChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @PostMapping("/chat")
    public ApiResponse<LearningChatResponse> chat(Authentication auth,
                                                   @Valid @RequestBody LearningChatRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(learningService.chat(userId, req));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<LearningSession>> sessions(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(learningService.listSessions(userId));
    }
}
