package com.example.sprout.domain.interview.sse.event;

import com.example.sprout.global.error.BusinessException;
import com.example.sprout.global.error.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SseEventStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // SSE 재연결 시 이벤트 복구를 위한 임시 저장 기간
    private static final Duration EVENT_TTL = Duration.ofHours(2);

    // 이벤트 저장 및 순차 ID 발급
    public StoredEvent append(Long sessionId, String eventName, Object data) {

        // Last-Event-ID 기반 재전송을 위해 세션별 이벤트 ID를 원자적으로 증가
        String seqKey = "sse:event-seq:" + sessionId;
        Long eventId = redisTemplate.opsForValue().increment(seqKey);
        redisTemplate.expire(seqKey, EVENT_TTL);

        // 재연결 시 복구할 수 있도록 이벤트를 저장 가능한 형태로 생성
        StoredEvent event = new StoredEvent(
                eventId, eventName, serialize(data)
        );

        String listKey = "sse:events:" + sessionId;

        // 재연결 시 Last-Event-ID 이후 이벤트를 조회할 수 있도록 순서대로 저장
        redisTemplate.opsForList().rightPush(listKey, serialize(event));
        redisTemplate.expire(listKey, EVENT_TTL);

        return event;
    }

    // 재연결 시 마지막으로 받은 이벤트 이후의 이벤트 조회
    public List<StoredEvent> getEventsAfter(Long sessionId, Long lastEventId) {
        String listKey = "sse:events:" + sessionId;

        List<String> raw = redisTemplate.opsForList().range(listKey, 0, -1);

        if (raw == null || raw.isEmpty()) {
            return List.of();
        }

        long threshold = (lastEventId != null) ? lastEventId : 0L;

        return raw.stream()
                .map(this::deserialize)
                .filter(event -> event.id() > threshold)
                .toList();
    }

    // 세션 종료 시 저장된 이벤트 삭제
    public void clear(Long sessionId) {
        redisTemplate.delete(List.of(
                "sse:events:" + sessionId,
                "sse:event-seq:" + sessionId
        ));
    }


    // SSE 이벤트 직렬화
    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JacksonException e) {
            throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // SSE 이벤트 역직렬화
    private StoredEvent deserialize(String json) {
        try {
            return objectMapper.readValue(json, StoredEvent.class);
        } catch (JacksonException e) {
            throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

}
