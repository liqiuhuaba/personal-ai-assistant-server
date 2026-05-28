package com.personalai.assistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.personalai.assistant")
@EnableScheduling
public class PersonalAiAssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonalAiAssistantApplication.class, args);
    }
}
