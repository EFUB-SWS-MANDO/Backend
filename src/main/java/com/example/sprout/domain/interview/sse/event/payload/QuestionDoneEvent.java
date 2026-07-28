package com.example.sprout.domain.interview.sse.event.payload;

public record QuestionDoneEvent(
        Long questionId,
        String question
) {
}
