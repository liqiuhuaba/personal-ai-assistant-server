package com.personalai.assistant.news;

import com.personalai.assistant.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/today")
    public ApiResponse<String> today() {
        return ApiResponse.ok(newsService.getTodayDigest());
    }
}
