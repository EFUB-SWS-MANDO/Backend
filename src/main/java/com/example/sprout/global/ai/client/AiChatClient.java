package com.example.sprout.global.ai.client;

import com.example.sprout.global.ai.dto.AiChatRequest;
import com.example.sprout.global.ai.dto.AiChatResponse;
import reactor.core.publisher.Flux;

public interface AiChatClient {
    AiChatResponse chat(AiChatRequest request);       // 동기 (분류, 자소서 초안)
    Flux<String> chatStream(AiChatRequest request);   // 스트리밍 (모의면접)
}