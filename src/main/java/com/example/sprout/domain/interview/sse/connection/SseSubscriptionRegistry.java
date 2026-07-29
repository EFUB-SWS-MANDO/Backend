package com.example.sprout.domain.interview.sse.connection;

import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseSubscriptionRegistry {

    // 세션별 응답 스트리밍 구독(Disposable) 관리
    private final Map<Long, Disposable> subscriptionsBySession = new ConcurrentHashMap<>();

    // 응답 스트리밍 구독 등록
    public void register(Long sessionId, Disposable disposable) {
        subscriptionsBySession.put(sessionId, disposable);
    }

    // 연결 종료 시 스트리밍 구독 해제
    public void cancel(Long sessionId) {
        Disposable disposable = subscriptionsBySession.remove(sessionId);

        // 아직 활성 상태인 경우 스트림 중단
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
