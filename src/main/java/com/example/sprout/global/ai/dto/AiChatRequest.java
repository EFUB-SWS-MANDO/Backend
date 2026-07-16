package com.example.sprout.global.ai.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AiChatRequest(
        List<AiMessage> messages,
        Double temperature,
        Integer maxTokens
) {
}
