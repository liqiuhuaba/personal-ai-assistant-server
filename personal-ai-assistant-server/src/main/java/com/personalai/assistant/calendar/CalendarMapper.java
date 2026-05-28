package com.personalai.assistant.calendar;

import com.personalai.assistant.calendar.domain.CalendarEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CalendarMapper {
    void insert(CalendarEvent event);
    List<CalendarEvent> findByUserAndRange(
        @Param("userId") Long userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to);
    void deleteById(@Param("id") Long id, @Param("userId") Long userId);
}
