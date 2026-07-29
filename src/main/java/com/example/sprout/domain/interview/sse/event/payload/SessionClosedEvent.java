package com.example.sprout.domain.interview.sse.event.payload;

import com.example.sprout.domain.interview.sse.enums.SseCloseReason;

public record SessionClosedEvent(
        SseCloseReason reason
) {
}
