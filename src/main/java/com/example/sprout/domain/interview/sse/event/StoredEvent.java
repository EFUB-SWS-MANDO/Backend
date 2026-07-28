package com.example.sprout.domain.interview.sse.event;

public record StoredEvent(
        Long id,
        String eventName,
        String payload
) {
}
