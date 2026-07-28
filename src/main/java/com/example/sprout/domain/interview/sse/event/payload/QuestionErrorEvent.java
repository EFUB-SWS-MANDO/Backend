package com.example.sprout.domain.interview.sse.event.payload;

public record QuestionErrorEvent(
        Long questionId,
        String message
) {
}
