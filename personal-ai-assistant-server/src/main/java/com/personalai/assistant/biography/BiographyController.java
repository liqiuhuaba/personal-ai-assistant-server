package com.personalai.assistant.biography;

import com.personalai.assistant.biography.domain.dto.BiographyChatResponse;
import com.personalai.assistant.biography.domain.dto.BiographyEventResponse;
import com.personalai.assistant.biography.domain.dto.GenerateBiographyResponse;
import com.personalai.assistant.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biography")
@RequiredArgsConstructor
public class BiographyController {

    private final BiographyService biographyService;

    @PostMapping("/chat")
    public ApiResponse<BiographyChatResponse> chat(
            Authentication auth,
            @RequestParam(required = false) Long sessionId,
            @RequestBody String message) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(biographyService.chat(userId, sessionId, message));
    }

    @GetMapping("/events")
    public ApiResponse<List<BiographyEventResponse>> events(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(biographyService.listEvents(userId));
    }

    @PostMapping("/generate")
    public ApiResponse<GenerateBiographyResponse> generate(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(biographyService.generateBiography(userId));
    }
}
