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
