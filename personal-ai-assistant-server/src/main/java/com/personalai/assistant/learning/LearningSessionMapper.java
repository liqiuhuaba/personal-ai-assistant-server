package com.personalai.assistant.learning;

import com.personalai.assistant.learning.domain.LearningSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface LearningSessionMapper {
    void insert(LearningSession session);
    List<LearningSession> findByUserId(@Param("userId") Long userId);
}
