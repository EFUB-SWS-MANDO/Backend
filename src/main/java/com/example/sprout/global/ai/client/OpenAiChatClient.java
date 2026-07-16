package com.example.sprout.global.ai.client;

import com.example.sprout.global.ai.dto.AiChatRequest;
import com.example.sprout.global.ai.dto.AiChatResponse;
import com.example.sprout.global.ai.exception.AiCallException;
import com.example.sprout.global.config.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiChatClient implements AiChatClient {

    private final WebClient openAiWebClient;
    private final OpenAiProperties props;
    private final ObjectMapper objectMapper;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        Map<String, Object> body = toRequestBody(request, false);

        JsonNode result = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToMono(JsonNode.class)
                .block(props.timeout().read());

        String content = result.at("/choices/0/message/content").asText();
        return new AiChatResponse(content);
    }

    @Override
    public Flux<String> chatStream(AiChatRequest request) {
        Map<String, Object> body = toRequestBody(request, true);

        return openAiWebClient.post()
                .uri("/chat/completions")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .mapNotNull(ServerSentEvent::data)
                .takeWhile(data -> !"[DONE]".equals(data))
                .mapNotNull(this::extractDeltaContent)
                .timeout(props.timeout().read())
                .onErrorMap(e -> !(e instanceof AiCallException),
                        e -> new AiCallException("AI 스트리밍 호출 실패", e));
    }

    private Map<String, Object> toRequestBody(AiChatRequest request, boolean stream) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", props.model());
        body.put("messages", request.messages());
        body.put("max_tokens", request.maxTokens() != null ? request.maxTokens() : props.defaultMaxToken());
        if (request.temperature() != null) {
            body.put("temperature", request.temperature());
        }
        body.put("stream", stream);
        return body;
    }

    private String extractDeltaContent(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode delta = node.at("/choices/0/delta/content");
            return delta.isMissingNode() ? null : delta.asText(); // mapNotNull이 null은 걸러줌
        } catch (Exception e) {
            throw new AiCallException("AI 응답 파싱 실패: " + json, e);
        }
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("응답 본문 없음")
                .flatMap(bodyText -> Mono.error(
                        new AiCallException("OpenAI 호출 실패 (status=" + response.statusCode() + "): " + bodyText)
                ));
    }
}