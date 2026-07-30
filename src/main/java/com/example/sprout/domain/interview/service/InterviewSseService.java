package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.sse.connection.SseConnection;
import com.example.sprout.domain.interview.sse.connection.SseEmitterRepository;
import com.example.sprout.domain.interview.sse.connection.SseSubscriptionRegistry;
import com.example.sprout.domain.interview.sse.enums.SseCloseReason;
import com.example.sprout.domain.interview.sse.event.SseEventStore;
import com.example.sprout.domain.interview.sse.event.StoredEvent;
import com.example.sprout.domain.interview.sse.event.payload.SessionClosedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InterviewSseService {

    // SSE 연결 유지 시간
    private static final long EMITTER_TIMEOUT_MS = Duration.ofHours(1).toMillis();

    private final SseEmitterRepository emitterRepository;
    private final SseEventStore eventStore;
    private final SseSubscriptionRegistry subscriptionRegistry;

    // SSE 연결 생성 및 재연결 시 누락된 이벤트 복구
    public SseEmitter connect(Long sessionId, Long lastEventId) {

        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);

        // 클라이언트(탭)별 마지막으로 전송한 이벤트 ID 관리
        SseConnection connection = new SseConnection(
                emitter, new AtomicLong(lastEventId != null? lastEventId : 0L)
        );

        // 연결 종료 시 emitter 정리
        emitter.onCompletion(() -> emitterRepository.remove(sessionId, connection));
        // 타임아웃 발생 시 연결 종료
        emitter.onTimeout(emitter::complete);
        // 오류 발생 시 emitter와 스트리밍 구독 모두 정리
        emitter.onError(e -> {
            emitterRepository.remove(sessionId, connection);
            subscriptionRegistry.cancel(sessionId);
        });

        // 활성 연결 등록
        emitterRepository.add(sessionId, connection);

        // Last-Event-ID 이후의 누락된 이벤트 재전송
        List<StoredEvent> missedEvents = eventStore.getEventsAfter(sessionId, lastEventId);

        for (StoredEvent event : missedEvents) {
            emitterRepository.sendTo(sessionId, connection, event);
        }

        return emitter;
    }

    // 이벤트를 저장한 뒤 연결된 모든 클라이언트(탭)에 실시간 전송
    public void publish(Long sessionId, String eventName, Object data) {
        StoredEvent event = eventStore.append(sessionId, eventName, data);
        emitterRepository.sendToAll(sessionId, event);
    }

    // 종료 이벤트 전송 후 연결과 저장된 이벤트 정리
    public void closeSession(Long sessionId, SseCloseReason reason) {
        publish(sessionId, "session-closed", new SessionClosedEvent(reason));
        emitterRepository.completeAll(sessionId);
        eventStore.clear(sessionId);
    }
}
