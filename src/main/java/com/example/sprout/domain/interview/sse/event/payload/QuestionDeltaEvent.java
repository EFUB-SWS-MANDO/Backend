package com.example.sprout.domain.interview.sse.event.payload;

public record QuestionDeltaEvent(
        Long questionId,
        String content
) {
}
