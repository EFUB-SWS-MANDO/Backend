package com.example.sprout.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "ai.openai")
public record OpenAiProperties(
        String apiKey,
        String baseUrl,
        String model,
        int defaultMaxToken,
        Timeout timeout
) {
    public record Timeout(Duration connect, Duration read) {}
}