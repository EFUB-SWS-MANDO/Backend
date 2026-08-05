package com.example.sprout.global.ai.dto;

public record AiChatResponse(
        String content,
        String finishReason,
        Integer reasoningTokens
) {
}
