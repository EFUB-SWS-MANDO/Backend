package com.example.sprout.domain.interview.sse.connection;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.atomic.AtomicLong;

public record SseConnection(
        SseEmitter emitter,
        AtomicLong lastSentEventId
) {
}
