package com.example.sprout.global.ai.dto;

public record AiChatResponse(
        String content,
        String finishReason,
        Integer reasoningTokens
) {
    // content만 필요한 기존 호출부(테스트 등) 호환용 - 정상 종료로 간주
    public AiChatResponse(String content) {
        this(content, "stop", null);
    }
}
