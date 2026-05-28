package com.personalai.assistant.calendar;

import com.personalai.assistant.calendar.domain.CalendarEvent;
import com.personalai.assistant.calendar.domain.dto.CreateEventRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock CalendarMapper calendarMapper;
    @InjectMocks CalendarService calendarService;

    @Test
    void createEvent_savesAndReturnsEvent() {
        var req = new CreateEventRequest("Team meeting",
            LocalDateTime.of(2026, 6, 1, 15, 0),
            LocalDateTime.of(2026, 6, 1, 16, 0),
            null, "manual");

        doAnswer(inv -> { ((CalendarEvent) inv.getArgument(0)).setId(1L); return null; })
            .when(calendarMapper).insert(any());

        var result = calendarService.createEvent(1L, req);

        assertThat(result.title()).isEqualTo("Team meeting");
        assertThat(result.id()).isEqualTo(1L);
        verify(calendarMapper).insert(any());
    }

    @Test
    void listEvents_returnsUserEvents() {
        var event = new CalendarEvent();
        event.setId(1L); event.setTitle("Stand-up");
        when(calendarMapper.findByUserAndRange(eq(1L), any(), any())).thenReturn(List.of(event));

        var results = calendarService.listEvents(1L,
            LocalDateTime.of(2026, 6, 1, 0, 0),
            LocalDateTime.of(2026, 6, 30, 23, 59));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo("Stand-up");
    }
}
