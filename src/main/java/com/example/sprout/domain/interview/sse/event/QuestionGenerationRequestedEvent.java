package com.example.sprout.domain.interview.sse.event;

import com.example.sprout.domain.interview.enums.InterviewSessionType;

import java.util.List;

public record QuestionGenerationRequestedEvent(
        Long sessionId,
        Long questionId,
        InterviewSessionType type,
        List<Long> targetIds
) {
}
