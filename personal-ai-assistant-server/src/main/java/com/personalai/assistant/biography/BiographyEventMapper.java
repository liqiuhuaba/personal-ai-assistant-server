package com.personalai.assistant.biography;

import com.personalai.assistant.biography.domain.BiographyEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BiographyEventMapper {
    void insert(BiographyEvent event);
    List<BiographyEvent> findByUserId(@Param("userId") Long userId);
}
