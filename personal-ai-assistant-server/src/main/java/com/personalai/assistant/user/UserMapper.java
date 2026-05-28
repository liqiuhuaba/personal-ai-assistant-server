package com.personalai.assistant.user;

import com.personalai.assistant.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    void insert(User user);
    User findByUsername(@Param("username") String username);
    User findById(@Param("id") Long id);
}
