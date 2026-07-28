package com.example.sprout.domain.interview.sse.event;

import com.example.sprout.domain.interview.enums.InterviewQuestionType;

public record NextQuestionGenerationRequestedEvent(
        Long sessionId,
        Long questionId,
        InterviewQuestionType questionType, // FOLLOW_UP or EXTRA
        String lastAnswer
) {
}
