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
