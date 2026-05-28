package com.personalai.assistant.chat;

import com.personalai.assistant.chat.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void insert(ChatMessage message);
    List<ChatMessage> findBySessionId(@Param("sessionId") Long sessionId);
}
