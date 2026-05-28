package com.personalai.assistant.common;

import com.personalai.assistant.auth.AuthService;
import com.personalai.assistant.auth.JwtAuthFilter;
import com.personalai.assistant.auth.JwtUtil;
import com.personalai.assistant.config.JwtProperties;
import com.personalai.assistant.config.SecurityConfig;
import com.personalai.assistant.user.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class, JwtProperties.class,
         GlobalExceptionHandlerTest.TestController.class})
@TestPropertySource(properties = {
    "jwt.secret=local-dev-jwt-secret-must-be-at-least-32-bytes-long!!",
    "jwt.expiration-ms=3600000"
})
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;
    @MockBean AuthService authService;
    @MockBean UserMapper userMapper;
    @SpyBean JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void allowFilterChain() throws Exception {
        doAnswer(inv -> {
            HttpServletRequest req = inv.getArgument(0);
            HttpServletResponse resp = inv.getArgument(1);
            FilterChain chain = inv.getArgument(2);
            chain.doFilter(req, resp);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser
    void bizException_returns400WithMessage() throws Exception {
        mockMvc.perform(get("/test/biz-error"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("test error"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/biz-error")
        String error() { throw new BizException("test error"); }
    }
}
