package com.personalai.assistant.calendar;

import com.personalai.assistant.calendar.domain.CalendarEvent;
import com.personalai.assistant.calendar.domain.dto.CreateEventRequest;
import com.personalai.assistant.calendar.domain.dto.EventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarMapper calendarMapper;

    public EventResponse createEvent(Long userId, CreateEventRequest req) {
        CalendarEvent event = new CalendarEvent();
        event.setUserId(userId);
        event.setTitle(req.title());
        event.setStartTime(req.startTime());
        event.setEndTime(req.endTime());
        event.setRemindAt(req.remindAt());
        event.setSource(req.source() != null ? req.source() : "manual");
        calendarMapper.insert(event);
        return toResponse(event);
    }

    public List<EventResponse> listEvents(Long userId, LocalDateTime from, LocalDateTime to) {
        return calendarMapper.findByUserAndRange(userId, from, to)
            .stream().map(this::toResponse).toList();
    }

    public void deleteEvent(Long userId, Long eventId) {
        calendarMapper.deleteById(eventId, userId);
    }

    private EventResponse toResponse(CalendarEvent e) {
        return new EventResponse(e.getId(), e.getTitle(),
            e.getStartTime(), e.getEndTime(), e.getRemindAt(), e.getSource());
    }
}
