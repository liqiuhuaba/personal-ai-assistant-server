package com.personalai.assistant.search;

import com.personalai.assistant.search.domain.SearchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SearchHistoryMapper {
    void insert(SearchHistory history);
    List<SearchHistory> findByUserId(@Param("userId") Long userId);
    void updateStarred(@Param("id") Long id, @Param("userId") Long userId, @Param("starred") boolean starred);
}
