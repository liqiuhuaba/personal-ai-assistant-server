package com.personalai.assistant.calendar;

import com.personalai.assistant.calendar.domain.dto.CreateEventRequest;
import com.personalai.assistant.calendar.domain.dto.EventResponse;
import com.personalai.assistant.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping("/events")
    public ApiResponse<EventResponse> create(Authentication auth,
                                             @Valid @RequestBody CreateEventRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(calendarService.createEvent(userId, req));
    }

    @GetMapping("/events")
    public ApiResponse<List<EventResponse>> list(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(calendarService.listEvents(userId, from, to));
    }

    @DeleteMapping("/events/{id}")
    public ApiResponse<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        calendarService.deleteEvent(userId, id);
        return ApiResponse.ok();
    }
}
