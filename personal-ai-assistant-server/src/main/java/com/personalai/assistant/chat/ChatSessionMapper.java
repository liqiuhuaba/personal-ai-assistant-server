package com.personalai.assistant.chat;

import com.personalai.assistant.chat.domain.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatSessionMapper {
    void insert(ChatSession session);
    List<ChatSession> findByUserId(@Param("userId") Long userId);
    ChatSession findById(@Param("id") Long id);
}
