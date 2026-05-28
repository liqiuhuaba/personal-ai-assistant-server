package com.personalai.assistant.search;

import com.personalai.assistant.common.ApiResponse;
import com.personalai.assistant.search.domain.SearchHistory;
import com.personalai.assistant.search.domain.dto.SearchRequest;
import com.personalai.assistant.search.domain.dto.SearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public ApiResponse<SearchResponse> search(Authentication auth,
                                              @Valid @RequestBody SearchRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(searchService.search(userId, req));
    }

    @GetMapping("/history")
    public ApiResponse<List<SearchHistory>> history(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(searchService.listHistory(userId));
    }

    @PutMapping("/history/{id}/star")
    public ApiResponse<Void> star(Authentication auth, @PathVariable Long id,
                                  @RequestParam boolean starred) {
        Long userId = (Long) auth.getPrincipal();
        searchService.toggleStar(userId, id, starred);
        return ApiResponse.ok();
    }
}
