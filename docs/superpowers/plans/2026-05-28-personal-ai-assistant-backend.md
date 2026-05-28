# Personal AI Assistant — Backend Implementation Plan (Part 1 of 2)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Java Spring Boot 3.x REST API backend powering auth, calendar, chat, biography, search, learning, and news features.

**Architecture:** Package-by-feature monolith. JWT stateless auth. OpenAI GPT-4o for AI features. Tavily REST API for web search. MySQL 8 for persistence, Redis for news digest cache.

**Tech Stack:** Java 17, Spring Boot 3.2.5, Spring Security 6, MyBatis 3.0.3, MySQL 8, Redis, JJWT 0.12.5, theokanning/openai-gpt3-java 0.18.2, JUnit 5, Mockito, MockMvc

---

## File Map

```
personal-ai-assistant-server/
├── pom.xml
├── src/main/java/com/personalai/assistant/
│   ├── PersonalAiAssistantApplication.java
│   ├── common/
│   │   ├── ApiResponse.java
│   │   ├── BizException.java
│   │   └── GlobalExceptionHandler.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtProperties.java
│   │   ├── OpenAiConfig.java
│   │   └── TavilyProperties.java
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthFilter.java
│   │   └── dto/ (LoginRequest, RegisterRequest, TokenResponse)
│   ├── user/
│   │   ├── UserMapper.java
│   │   └── domain/User.java
│   ├── calendar/
│   │   ├── CalendarController.java
│   │   ├── CalendarService.java
│   │   ├── CalendarMapper.java
│   │   └── domain/ (CalendarEvent, CreateEventRequest, EventResponse)
│   ├── chat/
│   │   ├── ChatController.java
│   │   ├── ChatService.java
│   │   ├── ChatSessionMapper.java
│   │   ├── ChatMessageMapper.java
│   │   └── domain/ (ChatSession, ChatMessage, ChatRequest, ChatResponse)
│   ├── biography/
│   │   ├── BiographyController.java
│   │   ├── BiographyService.java
│   │   ├── BiographyEventMapper.java
│   │   └── domain/ (BiographyEvent, BiographyEventResponse, GenerateBiographyResponse)
│   ├── search/
│   │   ├── SearchController.java
│   │   ├── SearchService.java
│   │   ├── TavilyClient.java
│   │   ├── SearchHistoryMapper.java
│   │   └── domain/ (SearchHistory, SearchRequest, SearchResponse)
│   ├── learning/
│   │   ├── LearningController.java
│   │   ├── LearningService.java
│   │   ├── LearningSessionMapper.java
│   │   └── domain/ (LearningSession, LearningChatRequest, LearningChatResponse)
│   └── news/
│       ├── NewsScheduler.java
│       ├── NewsService.java
│       └── NewsController.java
├── src/main/resources/
│   ├── application.yml
│   ├── db/schema.sql
│   └── mapper/ (*.xml for each Mapper)
└── src/test/java/com/personalai/assistant/
    ├── auth/AuthControllerTest.java
    ├── calendar/CalendarServiceTest.java
    ├── chat/ChatServiceTest.java
    ├── biography/BiographyServiceTest.java
    ├── search/SearchServiceTest.java
    └── news/NewsServiceTest.java
```

---

## Task 1: Project Scaffolding

**Files:**
- Create: `personal-ai-assistant-server/pom.xml`
- Create: `personal-ai-assistant-server/src/main/java/com/personalai/assistant/PersonalAiAssistantApplication.java`
- Create: `personal-ai-assistant-server/src/main/resources/application.yml`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p personal-ai-assistant-server/src/main/java/com/personalai/assistant
mkdir -p personal-ai-assistant-server/src/main/resources/db
mkdir -p personal-ai-assistant-server/src/main/resources/mapper
mkdir -p personal-ai-assistant-server/src/test/java/com/personalai/assistant
```

- [ ] **Step 2: Write pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
  </parent>
  <groupId>com.personalai</groupId>
  <artifactId>personal-ai-assistant-server</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>17</java.version>
  </properties>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-redis</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.mybatis.spring.boot</groupId><artifactId>mybatis-spring-boot-starter</artifactId><version>3.0.3</version></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.5</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.5</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.5</version><scope>runtime</scope></dependency>
    <dependency><groupId>com.theokanning.openai-gpt3-java</groupId><artifactId>service</artifactId><version>0.18.2</version></dependency>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 3: Write main application class**

`src/main/java/com/personalai/assistant/PersonalAiAssistantApplication.java`:
```java
package com.personalai.assistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.personalai.assistant.**.mapper")
@EnableScheduling
public class PersonalAiAssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonalAiAssistantApplication.class, args);
    }
}
```

- [ ] **Step 4: Write application.yml**

`src/main/resources/application.yml`:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_ai?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379

mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: your-256-bit-secret-key-replace-in-production-env
  expiration-ms: 86400000  # 24h

openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-4o

tavily:
  api-key: ${TAVILY_API_KEY}
  base-url: https://api.tavily.com
```

- [ ] **Step 5: Verify compile**

```bash
cd personal-ai-assistant-server && mvn compile
```
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add personal-ai-assistant-server/
git commit -m "feat: scaffold personal-ai-assistant-server Spring Boot project"
```

---

## Task 2: Database Schema

**Files:**
- Create: `src/main/resources/db/schema.sql`

- [ ] **Step 1: Write schema.sql**

```sql
CREATE DATABASE IF NOT EXISTS personal_ai CHARACTER SET utf8mb4;
USE personal_ai;

CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL UNIQUE,
  `password_hash` VARCHAR(128) NOT NULL,
  `avatar_url` VARCHAR(512),
  `cloud_sync` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `calendar_event` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(256) NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME,
  `remind_at` DATETIME,
  `source` ENUM('manual','ai') NOT NULL DEFAULT 'manual',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_start (`user_id`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `chat_session` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `mode` ENUM('chat','biography','learning') NOT NULL DEFAULT 'chat',
  `title` VARCHAR(256),
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_updated (`user_id`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `chat_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `session_id` BIGINT NOT NULL,
  `role` ENUM('user','assistant') NOT NULL,
  `content` TEXT NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `biography_event` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `event_date` VARCHAR(10),
  `title` VARCHAR(256) NOT NULL,
  `content` TEXT NOT NULL,
  `category` VARCHAR(64),
  `source_msg_id` BIGINT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_date (`user_id`, `event_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `learning_session` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `subject` VARCHAR(128) NOT NULL,
  `topic` VARCHAR(256),
  `score` INT,
  `duration_min` INT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `search_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `query` VARCHAR(512) NOT NULL,
  `summary` TEXT,
  `sources` JSON,
  `starred` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: Apply schema**

```bash
mysql -u root -p < src/main/resources/db/schema.sql
mysql -u root -p personal_ai -e "SHOW TABLES;"
```
Expected: lists all 6 tables.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/schema.sql
git commit -m "feat: add database schema for all modules"
```

---

## Task 3: Common Infrastructure

**Files:**
- Create: `src/main/java/com/personalai/assistant/common/ApiResponse.java`
- Create: `src/main/java/com/personalai/assistant/common/BizException.java`
- Create: `src/main/java/com/personalai/assistant/common/GlobalExceptionHandler.java`
- Test: `src/test/java/com/personalai/assistant/common/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Write failing test**

`src/test/java/com/personalai/assistant/common/GlobalExceptionHandlerTest.java`:
```java
package com.personalai.assistant.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;

    @Test
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
```

- [ ] **Step 2: Run test — expect FAIL**

```bash
mvn test -pl . -Dtest=GlobalExceptionHandlerTest -q 2>&1 | tail -5
```
Expected: FAIL — classes not found.

- [ ] **Step 3: Implement ApiResponse**

`src/main/java/com/personalai/assistant/common/ApiResponse.java`:
```java
package com.personalai.assistant.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "ok", data);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, "ok", null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

- [ ] **Step 4: Implement BizException**

`src/main/java/com/personalai/assistant/common/BizException.java`:
```java
package com.personalai.assistant.common;

public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
```

- [ ] **Step 5: Implement GlobalExceptionHandler**

`src/main/java/com/personalai/assistant/common/GlobalExceptionHandler.java`:
```java
package com.personalai.assistant.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBiz(BizException e) {
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneral(Exception e) {
        log.error("Unhandled exception", e);
        return ApiResponse.error("Internal server error");
    }
}
```

- [ ] **Step 6: Run test — expect PASS**

```bash
mvn test -pl . -Dtest=GlobalExceptionHandlerTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, 1 test passed.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/personalai/assistant/common/ src/test/
git commit -m "feat: add ApiResponse, BizException, GlobalExceptionHandler"
```

---

## Task 4: Auth Module (JWT Register + Login)

**Files:**
- Create: `config/JwtProperties.java`
- Create: `auth/JwtUtil.java`
- Create: `auth/JwtAuthFilter.java`
- Create: `config/SecurityConfig.java`
- Create: `user/domain/User.java`
- Create: `user/UserMapper.java` + `resources/mapper/UserMapper.xml`
- Create: `auth/dto/RegisterRequest.java`, `LoginRequest.java`, `TokenResponse.java`
- Create: `auth/AuthService.java`
- Create: `auth/AuthController.java`
- Test: `auth/AuthControllerTest.java`

- [ ] **Step 1: Write failing test**

`src/test/java/com/personalai/assistant/auth/AuthControllerTest.java`:
```java
package com.personalai.assistant.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalai.assistant.auth.dto.RegisterRequest;
import com.personalai.assistant.auth.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void register_thenLogin_returnsToken() throws Exception {
        var reg = new RegisterRequest("testuser_" + System.currentTimeMillis(), "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        var login = new LoginRequest(reg.username(), "password123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }
}
```

- [ ] **Step 2: Add test application.yml**

`src/test/resources/application-test.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_ai_test?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
jwt:
  secret: test-secret-key-at-least-256-bits-long-for-testing-only
  expiration-ms: 3600000
openai:
  api-key: test-key
  model: gpt-4o
tavily:
  api-key: test-key
  base-url: https://api.tavily.com
```

Also create the test DB:
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS personal_ai_test CHARACTER SET utf8mb4;"
mysql -u root -p personal_ai_test < src/main/resources/db/schema.sql
```

- [ ] **Step 3: Run test — expect FAIL**

```bash
mvn test -Dtest=AuthControllerTest -q 2>&1 | tail -5
```
Expected: FAIL — classes not found.

- [ ] **Step 4: Implement DTOs**

`auth/dto/RegisterRequest.java`:
```java
package com.personalai.assistant.auth.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegisterRequest(
    @NotBlank @Size(min=3, max=64) String username,
    @NotBlank @Size(min=6) String password
) {}
```

`auth/dto/LoginRequest.java`:
```java
package com.personalai.assistant.auth.dto;
import jakarta.validation.constraints.NotBlank;
public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
```

`auth/dto/TokenResponse.java`:
```java
package com.personalai.assistant.auth.dto;
public record TokenResponse(String token, long expiresIn) {}
```

- [ ] **Step 5: Implement User domain + UserMapper**

`user/domain/User.java`:
```java
package com.personalai.assistant.user.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String avatarUrl;
    private Boolean cloudSync;
    private LocalDateTime createdAt;
}
```

`user/UserMapper.java`:
```java
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
```

`resources/mapper/UserMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.personalai.assistant.user.UserMapper">
  <insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO `user` (username, password_hash, cloud_sync)
    VALUES (#{username}, #{passwordHash}, TRUE)
  </insert>
  <select id="findByUsername" resultType="com.personalai.assistant.user.domain.User">
    SELECT * FROM `user` WHERE username = #{username}
  </select>
  <select id="findById" resultType="com.personalai.assistant.user.domain.User">
    SELECT * FROM `user` WHERE id = #{id}
  </select>
</mapper>
```

- [ ] **Step 6: Implement JwtProperties + JwtUtil**

`config/JwtProperties.java`:
```java
package com.personalai.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expirationMs;
}
```

`auth/JwtUtil.java`:
```java
package com.personalai.assistant.auth;

import com.personalai.assistant.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;

    public String generate(Long userId) {
        var key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + props.getExpirationMs()))
            .signWith(key)
            .compact();
    }

    public Long parseUserId(String token) {
        var key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
        String subject = Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().getSubject();
        return Long.parseLong(subject);
    }
}
```

- [ ] **Step 7: Implement JwtAuthFilter**

`auth/JwtAuthFilter.java`:
```java
package com.personalai.assistant.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Long userId = jwtUtil.parseUserId(token);
                var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.debug("Invalid JWT: {}", e.getMessage());
            }
        }
        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 8: Implement SecurityConfig**

`config/SecurityConfig.java`:
```java
package com.personalai.assistant.config;

import com.personalai.assistant.auth.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 9: Implement AuthService + AuthController**

`auth/AuthService.java`:
```java
package com.personalai.assistant.auth;

import com.personalai.assistant.auth.dto.LoginRequest;
import com.personalai.assistant.auth.dto.RegisterRequest;
import com.personalai.assistant.auth.dto.TokenResponse;
import com.personalai.assistant.common.BizException;
import com.personalai.assistant.config.JwtProperties;
import com.personalai.assistant.user.UserMapper;
import com.personalai.assistant.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public void register(RegisterRequest req) {
        if (userMapper.findByUsername(req.username()) != null) {
            throw new BizException("Username already taken");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        userMapper.insert(user);
    }

    public TokenResponse login(LoginRequest req) {
        User user = userMapper.findByUsername(req.username());
        if (user == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BizException("Invalid username or password");
        }
        String token = jwtUtil.generate(user.getId());
        return new TokenResponse(token, jwtProperties.getExpirationMs());
    }
}
```

`auth/AuthController.java`:
```java
package com.personalai.assistant.auth;

import com.personalai.assistant.auth.dto.LoginRequest;
import com.personalai.assistant.auth.dto.RegisterRequest;
import com.personalai.assistant.auth.dto.TokenResponse;
import com.personalai.assistant.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }
}
```

- [ ] **Step 10: Run test — expect PASS**

```bash
mvn test -Dtest=AuthControllerTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, 1 test passed.

- [ ] **Step 11: Commit**

```bash
git add src/
git commit -m "feat: auth module — JWT register and login"
```

---

## Task 5: Calendar Module

**Files:**
- Create: `calendar/domain/CalendarEvent.java`
- Create: `calendar/domain/dto/CreateEventRequest.java`, `EventResponse.java`
- Create: `calendar/CalendarMapper.java` + `mapper/CalendarEventMapper.xml`
- Create: `calendar/CalendarService.java`
- Create: `calendar/CalendarController.java`
- Test: `calendar/CalendarServiceTest.java`

- [ ] **Step 1: Write failing test**

`src/test/java/com/personalai/assistant/calendar/CalendarServiceTest.java`:
```java
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
```

- [ ] **Step 2: Run test — expect FAIL**

```bash
mvn test -Dtest=CalendarServiceTest -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement Calendar domain**

`calendar/domain/CalendarEvent.java`:
```java
package com.personalai.assistant.calendar.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalendarEvent {
    private Long id;
    private Long userId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime remindAt;
    private String source;
    private LocalDateTime createdAt;
}
```

`calendar/domain/dto/CreateEventRequest.java`:
```java
package com.personalai.assistant.calendar.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateEventRequest(
    @NotBlank String title,
    @NotNull LocalDateTime startTime,
    LocalDateTime endTime,
    LocalDateTime remindAt,
    String source
) {}
```

`calendar/domain/dto/EventResponse.java`:
```java
package com.personalai.assistant.calendar.domain.dto;

import java.time.LocalDateTime;

public record EventResponse(
    Long id, String title,
    LocalDateTime startTime, LocalDateTime endTime,
    LocalDateTime remindAt, String source
) {}
```

- [ ] **Step 4: Implement CalendarMapper**

`calendar/CalendarMapper.java`:
```java
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
```

`resources/mapper/CalendarEventMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.personalai.assistant.calendar.CalendarMapper">
  <insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO calendar_event (user_id, title, start_time, end_time, remind_at, source)
    VALUES (#{userId}, #{title}, #{startTime}, #{endTime}, #{remindAt}, #{source})
  </insert>
  <select id="findByUserAndRange" resultType="com.personalai.assistant.calendar.domain.CalendarEvent">
    SELECT * FROM calendar_event
    WHERE user_id = #{userId} AND start_time BETWEEN #{from} AND #{to}
    ORDER BY start_time
  </select>
  <delete id="deleteById">
    DELETE FROM calendar_event WHERE id = #{id} AND user_id = #{userId}
  </delete>
</mapper>
```

- [ ] **Step 5: Implement CalendarService**

`calendar/CalendarService.java`:
```java
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
```

- [ ] **Step 6: Implement CalendarController**

`calendar/CalendarController.java`:
```java
package com.personalai.assistant.calendar;

import com.personalai.assistant.calendar.domain.dto.CreateEventRequest;
import com.personalai.assistant.calendar.domain.dto.EventResponse;
import com.personalai.assistant.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping("/events")
    public ApiResponse<EventResponse> create(Authentication auth,
                                             @Valid @RequestBody CreateEventRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(calendarService.createEvent(userId, req));
    }

    @GetMapping("/events")
    public ApiResponse<List<EventResponse>> list(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(calendarService.listEvents(userId, from, to));
    }

    @DeleteMapping("/events/{id}")
    public ApiResponse<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        calendarService.deleteEvent(userId, id);
        return ApiResponse.ok();
    }
}
```

- [ ] **Step 7: Run test — expect PASS**

```bash
mvn test -Dtest=CalendarServiceTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, 2 tests passed.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat: calendar module — CRUD events API"
```

---

## Task 6: Chat Module (Sessions + GPT Conversations)

**Files:**
- Create: `chat/domain/ChatSession.java`, `ChatMessage.java`
- Create: `chat/domain/dto/ChatRequest.java`, `ChatResponse.java`, `SessionResponse.java`
- Create: `chat/ChatSessionMapper.java` + `mapper/ChatSessionMapper.xml`
- Create: `chat/ChatMessageMapper.java` + `mapper/ChatMessageMapper.xml`
- Create: `config/OpenAiConfig.java`
- Create: `chat/ChatService.java`
- Create: `chat/ChatController.java`
- Test: `chat/ChatServiceTest.java`

- [ ] **Step 1: Write failing test**

`src/test/java/com/personalai/assistant/chat/ChatServiceTest.java`:
```java
package com.personalai.assistant.chat;

import com.personalai.assistant.chat.domain.ChatMessage;
import com.personalai.assistant.chat.domain.ChatSession;
import com.personalai.assistant.chat.domain.dto.ChatRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock ChatSessionMapper sessionMapper;
    @Mock ChatMessageMapper messageMapper;
    @Mock OpenAiService openAiService;
    @InjectMocks ChatService chatService;

    @Test
    void chat_createsSessionAndReturnsReply() {
        doAnswer(inv -> { ((ChatSession) inv.getArgument(0)).setId(1L); return null; })
            .when(sessionMapper).insert(any());
        doNothing().when(messageMapper).insert(any());

        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", "Hello back!");
        var mockChoice = new ChatCompletionChoice();
        mockChoice.setMessage(mockMsg);
        var mockResult = new ChatCompletionResult();
        mockResult.setChoices(List.of(mockChoice));
        when(openAiService.createChatCompletion(any())).thenReturn(mockResult);
        when(messageMapper.findBySessionId(1L)).thenReturn(List.of());

        var req = new ChatRequest(null, "chat", "Hello AI");
        var response = chatService.chat(1L, req);

        assertThat(response.reply()).isEqualTo("Hello back!");
        assertThat(response.sessionId()).isEqualTo(1L);
        verify(messageMapper, times(2)).insert(any()); // user msg + assistant msg
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

```bash
mvn test -Dtest=ChatServiceTest -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement OpenAiConfig**

`config/OpenAiConfig.java`:
```java
package com.personalai.assistant.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(apiKey, Duration.ofSeconds(60));
    }
}
```

- [ ] **Step 4: Implement Chat domain**

`chat/domain/ChatSession.java`:
```java
package com.personalai.assistant.chat.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSession {
    private Long id;
    private Long userId;
    private String mode;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

`chat/domain/ChatMessage.java`:
```java
package com.personalai.assistant.chat.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;
}
```

`chat/domain/dto/ChatRequest.java`:
```java
package com.personalai.assistant.chat.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record ChatRequest(Long sessionId, String mode, @NotBlank String message) {}
```

`chat/domain/dto/ChatResponse.java`:
```java
package com.personalai.assistant.chat.domain.dto;
public record ChatResponse(Long sessionId, String reply) {}
```

`chat/domain/dto/SessionResponse.java`:
```java
package com.personalai.assistant.chat.domain.dto;
import java.time.LocalDateTime;
public record SessionResponse(Long id, String mode, String title, LocalDateTime updatedAt) {}
```

- [ ] **Step 5: Implement Mappers**

`chat/ChatSessionMapper.java`:
```java
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
```

`chat/ChatMessageMapper.java`:
```java
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
```

`resources/mapper/ChatSessionMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.personalai.assistant.chat.ChatSessionMapper">
  <insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO chat_session (user_id, mode, title) VALUES (#{userId}, #{mode}, #{title})
  </insert>
  <select id="findByUserId" resultType="com.personalai.assistant.chat.domain.ChatSession">
    SELECT * FROM chat_session WHERE user_id = #{userId} ORDER BY updated_at DESC LIMIT 20
  </select>
  <select id="findById" resultType="com.personalai.assistant.chat.domain.ChatSession">
    SELECT * FROM chat_session WHERE id = #{id}
  </select>
</mapper>
```

`resources/mapper/ChatMessageMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.personalai.assistant.chat.ChatMessageMapper">
  <insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO chat_message (session_id, role, content) VALUES (#{sessionId}, #{role}, #{content})
  </insert>
  <select id="findBySessionId" resultType="com.personalai.assistant.chat.domain.ChatMessage">
    SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY created_at ASC
  </select>
</mapper>
```

- [ ] **Step 6: Implement ChatService**

`chat/ChatService.java`:
```java
package com.personalai.assistant.chat;

import com.personalai.assistant.chat.domain.ChatMessage;
import com.personalai.assistant.chat.domain.ChatSession;
import com.personalai.assistant.chat.domain.dto.ChatRequest;
import com.personalai.assistant.chat.domain.dto.ChatResponse;
import com.personalai.assistant.chat.domain.dto.SessionResponse;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final OpenAiService openAiService;

    @Value("${openai.model}")
    private String model;

    public ChatResponse chat(Long userId, ChatRequest req) {
        Long sessionId = req.sessionId();
        if (sessionId == null) {
            ChatSession session = new ChatSession();
            session.setUserId(userId);
            session.setMode(req.mode() != null ? req.mode() : "chat");
            session.setTitle(req.message().substring(0, Math.min(req.message().length(), 30)));
            sessionMapper.insert(session);
            sessionId = session.getId();
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(req.message());
        messageMapper.insert(userMsg);

        List<ChatMessage> history = messageMapper.findBySessionId(sessionId);
        List<com.theokanning.openai.completion.chat.ChatMessage> openAiMessages = history.stream()
            .map(m -> new com.theokanning.openai.completion.chat.ChatMessage(m.getRole(), m.getContent()))
            .collect(Collectors.toCollection(ArrayList::new));

        var completionReq = ChatCompletionRequest.builder()
            .model(model)
            .messages(openAiMessages)
            .build();

        String reply = openAiService.createChatCompletion(completionReq)
            .getChoices().get(0).getMessage().getContent();

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        messageMapper.insert(assistantMsg);

        return new ChatResponse(sessionId, reply);
    }

    public List<SessionResponse> listSessions(Long userId) {
        return sessionMapper.findByUserId(userId).stream()
            .map(s -> new SessionResponse(s.getId(), s.getMode(), s.getTitle(), s.getUpdatedAt()))
            .toList();
    }
}
```

- [ ] **Step 7: Implement ChatController**

`chat/ChatController.java`:
```java
package com.personalai.assistant.chat;

import com.personalai.assistant.chat.domain.dto.ChatRequest;
import com.personalai.assistant.chat.domain.dto.ChatResponse;
import com.personalai.assistant.chat.domain.dto.SessionResponse;
import com.personalai.assistant.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ApiResponse<ChatResponse> chat(Authentication auth,
                                          @Valid @RequestBody ChatRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(chatService.chat(userId, req));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionResponse>> sessions(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(chatService.listSessions(userId));
    }
}
```

- [ ] **Step 8: Run test — expect PASS**

```bash
mvn test -Dtest=ChatServiceTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, 1 test passed.

- [ ] **Step 9: Commit**

```bash
git add src/
git commit -m "feat: chat module — GPT-4o conversation sessions"
```

---

## Task 7: Biography Module (Dual-Task Event Extraction)

**Files:**
- Create: `biography/domain/BiographyEvent.java`
- Create: `biography/domain/dto/BiographyEventResponse.java`, `GenerateBiographyResponse.java`
- Create: `biography/BiographyEventMapper.java` + `mapper/BiographyEventMapper.xml`
- Create: `biography/BiographyService.java`
- Create: `biography/BiographyController.java`
- Test: `biography/BiographyServiceTest.java`

- [ ] **Step 1: Write failing test**

`src/test/java/com/personalai/assistant/biography/BiographyServiceTest.java`:
```java
package com.personalai.assistant.biography;

import com.personalai.assistant.biography.domain.BiographyEvent;
import com.personalai.assistant.chat.ChatMessageMapper;
import com.personalai.assistant.chat.ChatSessionMapper;
import com.personalai.assistant.chat.domain.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BiographyServiceTest {

    @Mock BiographyEventMapper biographyEventMapper;
    @Mock ChatSessionMapper sessionMapper;
    @Mock ChatMessageMapper messageMapper;
    @Mock OpenAiService openAiService;
    @InjectMocks BiographyService biographyService;

    @Test
    void chat_extractsBiographyEventsFromGptResponse() {
        doAnswer(inv -> { var s = (com.personalai.assistant.chat.domain.ChatSession) inv.getArgument(0); s.setId(1L); return null; })
            .when(sessionMapper).insert(any());
        doNothing().when(messageMapper).insert(any());
        when(messageMapper.findBySessionId(1L)).thenReturn(List.of());

        String gptReply = """
            {"reply":"那真是难忘的经历！","events":[{"event_date":"1998-09","title":"考入大学","content":"1998年9月考入北京某高校计算机系","category":"学业"}]}
            """;

        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", gptReply);
        var choice = new ChatCompletionChoice(); choice.setMessage(mockMsg);
        var result = new ChatCompletionResult(); result.setChoices(List.of(choice));
        when(openAiService.createChatCompletion(any())).thenReturn(result);
        doAnswer(inv -> { ((BiographyEvent) inv.getArgument(0)).setId(1L); return null; })
            .when(biographyEventMapper).insert(any());

        var response = biographyService.chat(1L, null, "我1998年考上大学了");

        assertThat(response.reply()).isEqualTo("那真是难忘的经历！");
        assertThat(response.extractedEvents()).hasSize(1);
        assertThat(response.extractedEvents().get(0).title()).isEqualTo("考入大学");
        verify(biographyEventMapper).insert(any());
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

```bash
mvn test -Dtest=BiographyServiceTest -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement BiographyEvent domain**

`biography/domain/BiographyEvent.java`:
```java
package com.personalai.assistant.biography.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BiographyEvent {
    private Long id;
    private Long userId;
    private String eventDate;
    private String title;
    private String content;
    private String category;
    private Long sourceMsgId;
    private LocalDateTime createdAt;
}
```

`biography/domain/dto/BiographyEventResponse.java`:
```java
package com.personalai.assistant.biography.domain.dto;
public record BiographyEventResponse(Long id, String eventDate, String title, String content, String category) {}
```

`biography/domain/dto/BiographyChatResponse.java`:
```java
package com.personalai.assistant.biography.domain.dto;
import java.util.List;
public record BiographyChatResponse(Long sessionId, String reply, List<BiographyEventResponse> extractedEvents) {}
```

`biography/domain/dto/GenerateBiographyResponse.java`:
```java
package com.personalai.assistant.biography.domain.dto;
public record GenerateBiographyResponse(String markdown) {}
```

- [ ] **Step 4: Implement BiographyEventMapper**

`biography/BiographyEventMapper.java`:
```java
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
```

`resources/mapper/BiographyEventMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.personalai.assistant.biography.BiographyEventMapper">
  <insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO biography_event (user_id, event_date, title, content, category, source_msg_id)
    VALUES (#{userId}, #{eventDate}, #{title}, #{content}, #{category}, #{sourceMsgId})
  </insert>
  <select id="findByUserId" resultType="com.personalai.assistant.biography.domain.BiographyEvent">
    SELECT * FROM biography_event WHERE user_id = #{userId} ORDER BY event_date ASC
  </select>
</mapper>
```

- [ ] **Step 5: Implement BiographyService**

`biography/BiographyService.java`:
```java
package com.personalai.assistant.biography;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalai.assistant.biography.domain.BiographyEvent;
import com.personalai.assistant.biography.domain.dto.BiographyChatResponse;
import com.personalai.assistant.biography.domain.dto.BiographyEventResponse;
import com.personalai.assistant.biography.domain.dto.GenerateBiographyResponse;
import com.personalai.assistant.chat.ChatMessageMapper;
import com.personalai.assistant.chat.ChatSessionMapper;
import com.personalai.assistant.chat.domain.ChatMessage;
import com.personalai.assistant.chat.domain.ChatSession;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiographyService {

    private final BiographyEventMapper biographyEventMapper;
    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    private static final String SYSTEM_PROMPT = """
        你是一个专门帮用户记录人生故事的AI助手。在陪用户聊过往经历时，你有两个任务：
        1. 给出温暖、有引导性的回复，鼓励用户继续讲述。
        2. 从用户的叙述中提取关键人生事件。
        
        你必须严格以JSON格式回复，格式如下：
        {"reply":"你的回复内容","events":[{"event_date":"YYYY或YYYY-MM或YYYY-MM-DD","title":"事件标题","content":"详细描述","category":"童年/学业/职业/家庭/其他"}]}
        如果没有可提取的事件，events数组为空。只输出JSON，不要有其他内容。
        """;

    public BiographyChatResponse chat(Long userId, Long sessionId, String message) {
        if (sessionId == null) {
            ChatSession session = new ChatSession();
            session.setUserId(userId);
            session.setMode("biography");
            session.setTitle(message.substring(0, Math.min(message.length(), 30)));
            sessionMapper.insert(session);
            sessionId = session.getId();
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        messageMapper.insert(userMsg);

        List<ChatMessage> history = messageMapper.findBySessionId(sessionId);
        List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();
        messages.add(new com.theokanning.openai.completion.chat.ChatMessage("system", SYSTEM_PROMPT));
        history.forEach(m -> messages.add(
            new com.theokanning.openai.completion.chat.ChatMessage(m.getRole(), m.getContent())));

        var req = ChatCompletionRequest.builder().model(model).messages(messages).build();
        String rawReply = openAiService.createChatCompletion(req)
            .getChoices().get(0).getMessage().getContent();

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(rawReply);
        messageMapper.insert(assistantMsg);

        String replyText = rawReply;
        List<BiographyEventResponse> extractedEvents = List.of();

        try {
            JsonNode node = objectMapper.readTree(rawReply);
            replyText = node.get("reply").asText();
            List<BiographyEvent> events = new ArrayList<>();
            for (JsonNode ev : node.get("events")) {
                BiographyEvent event = new BiographyEvent();
                event.setUserId(userId);
                event.setEventDate(ev.get("event_date").asText());
                event.setTitle(ev.get("title").asText());
                event.setContent(ev.get("content").asText());
                event.setCategory(ev.get("category").asText());
                event.setSourceMsgId(assistantMsg.getId());
                biographyEventMapper.insert(event);
                events.add(event);
            }
            extractedEvents = events.stream()
                .map(e -> new BiographyEventResponse(e.getId(), e.getEventDate(),
                    e.getTitle(), e.getContent(), e.getCategory()))
                .toList();
        } catch (Exception e) {
            log.warn("Failed to parse biography GPT response as JSON, using raw reply: {}", e.getMessage());
        }

        return new BiographyChatResponse(sessionId, replyText, extractedEvents);
    }

    public List<BiographyEventResponse> listEvents(Long userId) {
        return biographyEventMapper.findByUserId(userId).stream()
            .map(e -> new BiographyEventResponse(e.getId(), e.getEventDate(),
                e.getTitle(), e.getContent(), e.getCategory()))
            .toList();
    }

    public GenerateBiographyResponse generateBiography(Long userId) {
        List<BiographyEvent> events = biographyEventMapper.findByUserId(userId);
        if (events.isEmpty()) {
            return new GenerateBiographyResponse("# 我的传记\n\n还没有记录任何人生事件，请先在传记模式中聊聊你的过往经历。");
        }

        String eventsText = events.stream()
            .map(e -> String.format("- [%s] %s: %s", e.getEventDate(), e.getTitle(), e.getContent()))
            .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            请根据以下人生事件，以第一人称撰写一篇完整的个人传记草稿，Markdown格式，语言温暖真实：
            
            %s
            """, eventsText);

        var req = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(new com.theokanning.openai.completion.chat.ChatMessage("user", prompt)))
            .build();

        String markdown = openAiService.createChatCompletion(req)
            .getChoices().get(0).getMessage().getContent();

        return new GenerateBiographyResponse(markdown);
    }
}
```

- [ ] **Step 6: Implement BiographyController**

`biography/BiographyController.java`:
```java
package com.personalai.assistant.biography;

import com.personalai.assistant.biography.domain.dto.BiographyChatResponse;
import com.personalai.assistant.biography.domain.dto.BiographyEventResponse;
import com.personalai.assistant.biography.domain.dto.GenerateBiographyResponse;
import com.personalai.assistant.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biography")
@RequiredArgsConstructor
public class BiographyController {

    private final BiographyService biographyService;

    @PostMapping("/chat")
    public ApiResponse<BiographyChatResponse> chat(
            Authentication auth,
            @RequestParam(required = false) Long sessionId,
            @RequestBody String message) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(biographyService.chat(userId, sessionId, message));
    }

    @GetMapping("/events")
    public ApiResponse<List<BiographyEventResponse>> events(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(biographyService.listEvents(userId));
    }

    @PostMapping("/generate")
    public ApiResponse<GenerateBiographyResponse> generate(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(biographyService.generateBiography(userId));
    }
}
```

- [ ] **Step 7: Run test — expect PASS**

```bash
mvn test -Dtest=BiographyServiceTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, 1 test passed.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat: biography module — dual-task GPT event extraction and generation"
```

---

## Task 8: Search Module (Tavily + GPT Summarization)

**Files:**
- Create: `config/TavilyProperties.java`
- Create: `search/TavilyClient.java`
- Create: `search/domain/SearchHistory.java`
- Create: `search/domain/dto/SearchRequest.java`, `SearchResponse.java`
- Create: `search/SearchHistoryMapper.java` + `mapper/SearchHistoryMapper.xml`
- Create: `search/SearchService.java`
- Create: `search/SearchController.java`
- Test: `search/SearchServiceTest.java`

- [ ] **Step 1: Write failing test**

`src/test/java/com/personalai/assistant/search/SearchServiceTest.java`:
```java
package com.personalai.assistant.search;

import com.personalai.assistant.search.domain.dto.SearchRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock TavilyClient tavilyClient;
    @Mock SearchHistoryMapper searchHistoryMapper;
    @Mock OpenAiService openAiService;
    @InjectMocks SearchService searchService;

    @Test
    void search_callsTavilyThenGptAndSavesHistory() {
        when(tavilyClient.search("GPT-5发布")).thenReturn(List.of(
            new TavilyClient.TavilyResult("GPT-5已发布", "https://openai.com", "OpenAI宣布发布GPT-5...")
        ));

        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", "GPT-5已于近日正式发布。");
        var choice = new ChatCompletionChoice(); choice.setMessage(mockMsg);
        var result = new ChatCompletionResult(); result.setChoices(List.of(choice));
        when(openAiService.createChatCompletion(any())).thenReturn(result);
        doNothing().when(searchHistoryMapper).insert(any());

        var response = searchService.search(1L, new SearchRequest("GPT-5发布", null));

        assertThat(response.summary()).isEqualTo("GPT-5已于近日正式发布。");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).url()).isEqualTo("https://openai.com");
        verify(searchHistoryMapper).insert(any());
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

```bash
mvn test -Dtest=SearchServiceTest -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement TavilyProperties + TavilyClient**

`config/TavilyProperties.java`:
```java
package com.personalai.assistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tavily")
public class TavilyProperties {
    private String apiKey;
    private String baseUrl;
}
```

`search/TavilyClient.java`:
```java
package com.personalai.assistant.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.personalai.assistant.config.TavilyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TavilyClient {

    private final TavilyProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<TavilyResult> search(String query) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = Map.of("api_key", props.getApiKey(), "query", query, "max_results", 5);
        var entity = new HttpEntity<>(body, headers);
        var response = restTemplate.postForObject(
            props.getBaseUrl() + "/search", entity, TavilyResponse.class);
        return response != null && response.results() != null ? response.results() : List.of();
    }

    public record TavilyResult(String title, String url, String content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TavilyResponse(@JsonProperty("results") List<TavilyResult> results) {}
}
```

- [ ] **Step 4: Implement Search domain + Mapper**

`search/domain/SearchHistory.java`:
```java
package com.personalai.assistant.search.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SearchHistory {
    private Long id;
    private Long userId;
    private String query;
    private String summary;
    private String sources;
    private Boolean starred;
    private LocalDateTime createdAt;
}
```

`search/domain/dto/SearchRequest.java`:
```java
package com.personalai.assistant.search.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record SearchRequest(@NotBlank String query, Long sessionId) {}
```

`search/domain/dto/SearchResponse.java`:
```java
package com.personalai.assistant.search.domain.dto;
import java.util.List;
public record SearchResponse(String summary, List<SourceItem> sources, Long historyId) {
    public record SourceItem(String title, String url) {}
}
```

`search/SearchHistoryMapper.java`:
```java
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
```

`resources/mapper/SearchHistoryMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.personalai.assistant.search.SearchHistoryMapper">
  <insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO search_history (user_id, query, summary, sources, starred)
    VALUES (#{userId}, #{query}, #{summary}, #{sources}, FALSE)
  </insert>
  <select id="findByUserId" resultType="com.personalai.assistant.search.domain.SearchHistory">
    SELECT * FROM search_history WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 50
  </select>
  <update id="updateStarred">
    UPDATE search_history SET starred = #{starred} WHERE id = #{id} AND user_id = #{userId}
  </update>
</mapper>
```

- [ ] **Step 5: Implement SearchService**

`search/SearchService.java`:
```java
package com.personalai.assistant.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalai.assistant.search.domain.SearchHistory;
import com.personalai.assistant.search.domain.dto.SearchRequest;
import com.personalai.assistant.search.domain.dto.SearchResponse;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final TavilyClient tavilyClient;
    private final SearchHistoryMapper searchHistoryMapper;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    public SearchResponse search(Long userId, SearchRequest req) {
        List<TavilyClient.TavilyResult> results = tavilyClient.search(req.query());

        String context = results.stream()
            .map(r -> String.format("标题：%s\n内容：%s\n来源：%s", r.title(), r.content(), r.url()))
            .collect(Collectors.joining("\n\n"));

        String prompt = String.format("""
            用户问题：%s
            
            以下是搜索到的相关内容：
            %s
            
            请用简洁清晰的中文汇总回答用户问题，并在末尾标注信息来源序号。
            """, req.query(), context);

        var chatReq = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(new com.theokanning.openai.completion.chat.ChatMessage("user", prompt)))
            .build();

        String summary = openAiService.createChatCompletion(chatReq)
            .getChoices().get(0).getMessage().getContent();

        List<SearchResponse.SourceItem> sources = results.stream()
            .map(r -> new SearchResponse.SourceItem(r.title(), r.url()))
            .toList();

        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setQuery(req.query());
        history.setSummary(summary);
        try {
            history.setSources(objectMapper.writeValueAsString(sources));
        } catch (Exception e) {
            log.warn("Failed to serialize sources", e);
        }
        searchHistoryMapper.insert(history);

        return new SearchResponse(summary, sources, history.getId());
    }

    public List<SearchHistory> listHistory(Long userId) {
        return searchHistoryMapper.findByUserId(userId);
    }

    public void toggleStar(Long userId, Long historyId, boolean starred) {
        searchHistoryMapper.updateStarred(historyId, userId, starred);
    }
}
```

- [ ] **Step 6: Implement SearchController**

`search/SearchController.java`:
```java
package com.personalai.assistant.search;

import com.personalai.assistant.common.ApiResponse;
import com.personalai.assistant.search.domain.SearchHistory;
import com.personalai.assistant.search.domain.dto.SearchRequest;
import com.personalai.assistant.search.domain.dto.SearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public ApiResponse<SearchResponse> search(Authentication auth,
                                              @Valid @RequestBody SearchRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(searchService.search(userId, req));
    }

    @GetMapping("/history")
    public ApiResponse<List<SearchHistory>> history(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(searchService.listHistory(userId));
    }

    @PutMapping("/history/{id}/star")
    public ApiResponse<Void> star(Authentication auth, @PathVariable Long id,
                                  @RequestParam boolean starred) {
        Long userId = (Long) auth.getPrincipal();
        searchService.toggleStar(userId, id, starred);
        return ApiResponse.ok();
    }
}
```

- [ ] **Step 7: Run test — expect PASS**

```bash
mvn test -Dtest=SearchServiceTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, 1 test passed.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat: search module — Tavily + GPT summarization with history"
```

---

## Task 9: Learning Module

**Files:**
- Create: `learning/domain/LearningSession.java`
- Create: `learning/domain/dto/LearningChatRequest.java`, `LearningChatResponse.java`
- Create: `learning/LearningSessionMapper.java` + `mapper/LearningSessionMapper.xml`
- Create: `learning/LearningService.java`
- Create: `learning/LearningController.java`
- Test: `learning/LearningServiceTest.java`

- [ ] **Step 1: Write failing test**

`src/test/java/com/personalai/assistant/learning/LearningServiceTest.java`:
```java
package com.personalai.assistant.learning;

import com.personalai.assistant.chat.ChatMessageMapper;
import com.personalai.assistant.chat.ChatSessionMapper;
import com.personalai.assistant.chat.domain.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningServiceTest {

    @Mock LearningSessionMapper learningSessionMapper;
    @Mock ChatSessionMapper chatSessionMapper;
    @Mock ChatMessageMapper messageMapper;
    @Mock OpenAiService openAiService;
    @InjectMocks LearningService learningService;

    @Test
    void chat_returnsAiTutorReply() {
        doAnswer(inv -> { var s = (com.personalai.assistant.chat.domain.ChatSession) inv.getArgument(0); s.setId(1L); return null; })
            .when(chatSessionMapper).insert(any());
        doNothing().when(messageMapper).insert(any());
        when(messageMapper.findBySessionId(1L)).thenReturn(List.of());

        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", "Great question! The answer is 42.");
        var choice = new ChatCompletionChoice(); choice.setMessage(mockMsg);
        var result = new ChatCompletionResult(); result.setChoices(List.of(choice));
        when(openAiService.createChatCompletion(any())).thenReturn(result);

        var req = new com.personalai.assistant.learning.domain.dto.LearningChatRequest(null, "数学", "高中代数", "什么是二次方程？");
        var response = learningService.chat(1L, req);

        assertThat(response.reply()).isEqualTo("Great question! The answer is 42.");
        assertThat(response.sessionId()).isEqualTo(1L);
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

```bash
mvn test -Dtest=LearningServiceTest -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement Learning domain**

`learning/domain/LearningSession.java`:
```java
package com.personalai.assistant.learning.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LearningSession {
    private Long id;
    private Long userId;
    private String subject;
    private String topic;
    private Integer score;
    private Integer durationMin;
    private LocalDateTime createdAt;
}
```

`learning/domain/dto/LearningChatRequest.java`:
```java
package com.personalai.assistant.learning.domain.dto;
import jakarta.validation.constraints.NotBlank;
public record LearningChatRequest(Long sessionId, @NotBlank String subject, String topic, @NotBlank String message) {}
```

`learning/domain/dto/LearningChatResponse.java`:
```java
package com.personalai.assistant.learning.domain.dto;
public record LearningChatResponse(Long sessionId, String reply) {}
```

- [ ] **Step 4: Implement LearningSessionMapper**

`learning/LearningSessionMapper.java`:
```java
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
```

`resources/mapper/LearningSessionMapper.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.personalai.assistant.learning.LearningSessionMapper">
  <insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO learning_session (user_id, subject, topic) VALUES (#{userId}, #{subject}, #{topic})
  </insert>
  <select id="findByUserId" resultType="com.personalai.assistant.learning.domain.LearningSession">
    SELECT * FROM learning_session WHERE user_id = #{userId} ORDER BY created_at DESC
  </select>
</mapper>
```

- [ ] **Step 5: Implement LearningService + LearningController**

`learning/LearningService.java`:
```java
package com.personalai.assistant.learning;

import com.personalai.assistant.chat.ChatMessageMapper;
import com.personalai.assistant.chat.ChatSessionMapper;
import com.personalai.assistant.chat.domain.ChatMessage;
import com.personalai.assistant.chat.domain.ChatSession;
import com.personalai.assistant.learning.domain.LearningSession;
import com.personalai.assistant.learning.domain.dto.LearningChatRequest;
import com.personalai.assistant.learning.domain.dto.LearningChatResponse;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningService {

    private final LearningSessionMapper learningSessionMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper messageMapper;
    private final OpenAiService openAiService;

    @Value("${openai.model}")
    private String model;

    public LearningChatResponse chat(Long userId, LearningChatRequest req) {
        Long sessionId = req.sessionId();
        if (sessionId == null) {
            ChatSession chatSession = new ChatSession();
            chatSession.setUserId(userId);
            chatSession.setMode("learning");
            chatSession.setTitle(req.subject() + " · " + (req.topic() != null ? req.topic() : ""));
            chatSessionMapper.insert(chatSession);
            sessionId = chatSession.getId();

            LearningSession ls = new LearningSession();
            ls.setUserId(userId);
            ls.setSubject(req.subject());
            ls.setTopic(req.topic());
            learningSessionMapper.insert(ls);
        }

        String systemPrompt = String.format("""
            你是一个专业的学习陪练AI，专注于 %s 科目%s。
            你的职责是：出题考察、耐心讲解、追踪理解程度，用鼓励的语气帮助用户学习。
            对用户的回答给出正误判断和详细解析。
            """, req.subject(), req.topic() != null ? "中的 " + req.topic() + " 主题" : "");

        List<ChatMessage> history = messageMapper.findBySessionId(sessionId);
        List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();
        messages.add(new com.theokanning.openai.completion.chat.ChatMessage("system", systemPrompt));
        history.forEach(m -> messages.add(
            new com.theokanning.openai.completion.chat.ChatMessage(m.getRole(), m.getContent())));
        messages.add(new com.theokanning.openai.completion.chat.ChatMessage("user", req.message()));

        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(req.message());
        messageMapper.insert(userMsg);

        var completionReq = ChatCompletionRequest.builder().model(model).messages(messages).build();
        String reply = openAiService.createChatCompletion(completionReq)
            .getChoices().get(0).getMessage().getContent();

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(reply);
        messageMapper.insert(assistantMsg);

        return new LearningChatResponse(sessionId, reply);
    }

    public List<LearningSession> listSessions(Long userId) {
        return learningSessionMapper.findByUserId(userId);
    }
}
```

`learning/LearningController.java`:
```java
package com.personalai.assistant.learning;

import com.personalai.assistant.common.ApiResponse;
import com.personalai.assistant.learning.domain.LearningSession;
import com.personalai.assistant.learning.domain.dto.LearningChatRequest;
import com.personalai.assistant.learning.domain.dto.LearningChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @PostMapping("/chat")
    public ApiResponse<LearningChatResponse> chat(Authentication auth,
                                                   @Valid @RequestBody LearningChatRequest req) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(learningService.chat(userId, req));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<LearningSession>> sessions(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ApiResponse.ok(learningService.listSessions(userId));
    }
}
```

- [ ] **Step 6: Run test — expect PASS**

```bash
mvn test -Dtest=LearningServiceTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, 1 test passed.

- [ ] **Step 7: Commit**

```bash
git add src/
git commit -m "feat: learning module — AI tutor chat sessions"
```

---

## Task 10: News Scheduler + Dashboard Endpoint

**Files:**
- Create: `news/NewsService.java`
- Create: `news/NewsScheduler.java`
- Create: `news/NewsController.java`
- Test: `news/NewsServiceTest.java`

- [ ] **Step 1: Write failing test**

`src/test/java/com/personalai/assistant/news/NewsServiceTest.java`:
```java
package com.personalai.assistant.news;

import com.personalai.assistant.search.TavilyClient;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.service.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock TavilyClient tavilyClient;
    @Mock OpenAiService openAiService;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOps;
    @InjectMocks NewsService newsService;

    @Test
    void refresh_fetchesAndCachesDigest() {
        when(tavilyClient.search(anyString())).thenReturn(List.of(
            new TavilyClient.TavilyResult("AI新闻", "https://example.com", "今日AI大事...")
        ));
        var mockMsg = new com.theokanning.openai.completion.chat.ChatMessage("assistant", "今日要闻：AI领域重大进展...");
        var choice = new ChatCompletionChoice(); choice.setMessage(mockMsg);
        var result = new ChatCompletionResult(); result.setChoices(List.of(choice));
        when(openAiService.createChatCompletion(any())).thenReturn(result);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        doNothing().when(valueOps).set(anyString(), anyString(), any());

        newsService.refreshDigest();

        verify(valueOps).set(eq("news:today"), anyString(), any(Duration.class));
    }

    @Test
    void getTodayDigest_returnsCachedValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("news:today")).thenReturn("今日要闻：...");

        String digest = newsService.getTodayDigest();

        assertThat(digest).isEqualTo("今日要闻：...");
    }
}
```

- [ ] **Step 2: Run test — expect FAIL**

```bash
mvn test -Dtest=NewsServiceTest -q 2>&1 | tail -5
```

- [ ] **Step 3: Implement NewsService**

`news/NewsService.java`:
```java
package com.personalai.assistant.news;

import com.personalai.assistant.search.TavilyClient;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final TavilyClient tavilyClient;
    private final OpenAiService openAiService;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY = "news:today";

    @Value("${openai.model}")
    private String model;

    public void refreshDigest() {
        log.info("Refreshing news digest...");
        List<TavilyClient.TavilyResult> results = tavilyClient.search("今日重点新闻 科技 财经 社会");

        String context = results.stream()
            .map(r -> String.format("标题：%s\n摘要：%s", r.title(), r.content()))
            .collect(Collectors.joining("\n\n"));

        String prompt = String.format("""
            请从以下搜索结果中提炼今日重点新闻，以简洁的要点形式呈现（3-5条），每条不超过50字：
            
            %s
            """, context);

        var req = ChatCompletionRequest.builder()
            .model(model)
            .messages(List.of(new com.theokanning.openai.completion.chat.ChatMessage("user", prompt)))
            .build();

        String digest = openAiService.createChatCompletion(req)
            .getChoices().get(0).getMessage().getContent();

        redisTemplate.opsForValue().set(REDIS_KEY, digest, Duration.ofHours(25));
        log.info("News digest refreshed and cached.");
    }

    public String getTodayDigest() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        return cached != null ? cached : "今日新闻正在加载中，请稍后刷新...";
    }
}
```

- [ ] **Step 4: Implement NewsScheduler**

`news/NewsScheduler.java`:
```java
package com.personalai.assistant.news;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final NewsService newsService;

    @Scheduled(cron = "0 0 7 * * *")
    public void dailyRefresh() {
        log.info("Scheduled news refresh triggered");
        newsService.refreshDigest();
    }
}
```

- [ ] **Step 5: Implement NewsController**

`news/NewsController.java`:
```java
package com.personalai.assistant.news;

import com.personalai.assistant.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/today")
    public ApiResponse<String> today() {
        return ApiResponse.ok(newsService.getTodayDigest());
    }
}
```

- [ ] **Step 6: Run test — expect PASS**

```bash
mvn test -Dtest=NewsServiceTest -q 2>&1 | tail -5
```
Expected: `BUILD SUCCESS`, 2 tests passed.

- [ ] **Step 7: Run all tests**

```bash
mvn test -q 2>&1 | tail -10
```
Expected: All tests pass.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat: news module — daily Tavily digest + Redis cache + dashboard endpoint"
```

---

## API Summary

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | 无 | 注册 |
| POST | `/api/auth/login` | 无 | 登录，返回 JWT |
| POST | `/api/calendar/events` | JWT | 创建日程 |
| GET | `/api/calendar/events?from=&to=` | JWT | 查询日程 |
| DELETE | `/api/calendar/events/{id}` | JWT | 删除日程 |
| POST | `/api/chat` | JWT | 普通对话 |
| GET | `/api/chat/sessions` | JWT | 对话历史 |
| POST | `/api/biography/chat` | JWT | 传记模式对话 |
| GET | `/api/biography/events` | JWT | 传记时间轴 |
| POST | `/api/biography/generate` | JWT | 生成传记草稿 |
| POST | `/api/search` | JWT | 联网搜索 |
| GET | `/api/search/history` | JWT | 搜索历史 |
| PUT | `/api/search/history/{id}/star` | JWT | 收藏搜索 |
| POST | `/api/learning/chat` | JWT | 学习陪练对话 |
| GET | `/api/learning/sessions` | JWT | 学习记录 |
| GET | `/api/news/today` | JWT | 今日新闻摘要 |
