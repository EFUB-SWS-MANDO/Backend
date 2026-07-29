package com.example.sprout.domain.interview.sse.scheduler;

import com.example.sprout.domain.interview.sse.connection.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final SseEmitterRepository emitterRepository;

    @Scheduled(fixedRate = 15000) // 15초마다
    public void sendHeartbeat() {
        emitterRepository.sendHeartbeatToAll();
    }
}
