package com.example.sprout.domain.interview.sse.connection;

import com.example.sprout.domain.interview.sse.event.StoredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class SseEmitterRepository {

    // 세션 하나에 여러 탭(SseEmitter) 연결 가능 -> 멀티 탭 지원
    private final Map<Long, List<SseConnection>> connectionsBySession = new ConcurrentHashMap<>();


    // 새로운 SSE 연결 등록
    public void add(Long sessionId, SseConnection connection) {
        connectionsBySession.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(connection);
    }

    // 연결 종료 시 해당 세션의 활성 연결 목록에서 제거
    public void remove(Long sessionId, SseConnection connection) {
        List<SseConnection> connections = connectionsBySession.get(sessionId);

        if (connections == null) {
            return;
        }
        connections.remove(connection);

        // 더 이상 활성 연결이 없으면 세션 정보도 함께 제거
        if (connections.isEmpty()) {
            connectionsBySession.remove(sessionId);
        }
    }

    // 세션에 연결된 모든 클라이언트(탭)에게 동일한 이벤트 전송
    public void sendToAll(Long sessionId, StoredEvent event) {
        for (SseConnection connection : connectionsBySession.getOrDefault(sessionId, List.of())) {
            sendTo(sessionId, connection, event);
        }
    }

    // 이벤트를 한 번만 순서대로 전송해 중복 및 순서 꼬임 방지
    public void sendTo(Long sessionId, SseConnection connection, StoredEvent event) {

        // replay와 실시간 전송이 동시에 같은 emitter 사용하는 상황 방지
        synchronized (connection.emitter()) {
            // 이미 전송한 이벤트는 중복 전송 X
            if (event.id() <= connection.lastSentEventId().get()) {
                return;
            }

            try {
                connection.emitter().send(SseEmitter.event()
                        .id(String.valueOf(event.id()))
                        .name(event.eventName())
                        .data(event.payload()));
                // 마지막으로 전송한 이벤트 ID 갱신
                connection.lastSentEventId().set(event.id());
            } catch (IOException e) {
                // 전송 실패한 연결은 (죽은 연결) 제거
                remove(sessionId, connection);
            }
        }
    }

    // 인터뷰 종료 시 모든 SSE 연결 종료
    public void completeAll(Long sessionId) {
        List<SseConnection> connections = connectionsBySession.remove(sessionId);
        if (connections == null) {
            return;
        }
        for (SseConnection connection : connections) {
            connection.emitter().complete();
        }
    }

    // 프록시(Nginx, Caddy 등)의 idle timeout으로 연결이 끊어지지 않도록 heartbeat 전송
    public void sendHeartbeatToAll() {
        connectionsBySession.forEach((sessionId, connections) -> {
            for (SseConnection connection : connections) {
                // heartbeat와 이벤트 전송이 동시에 발생하는 경우 방지
                synchronized (connection.emitter()) {
                    try {
                        connection.emitter().send(SseEmitter.event().comment("ping"));
                    } catch (IOException e) {
                        // heartbeat 전송 실패 시 연결 제거
                        remove(sessionId, connection);
                    }
                }
            }
        });
    }

}
