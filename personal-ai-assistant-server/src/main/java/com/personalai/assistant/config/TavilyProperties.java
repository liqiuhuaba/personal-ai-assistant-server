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
