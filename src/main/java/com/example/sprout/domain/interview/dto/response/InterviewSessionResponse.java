package com.example.sprout.domain.interview.dto.response;

import com.example.sprout.domain.interview.entity.InterviewQuestion;
import com.example.sprout.domain.interview.entity.InterviewSession;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InterviewSessionResponse(
        Long interviewSessionId,
        Long questionId,
        String sseTicket,
        LocalDateTime createdAt
) {
    public static InterviewSessionResponse from(
            InterviewSession interviewSession,
            InterviewQuestion interviewQuestion,
            String sseTicket
    ) {
        return InterviewSessionResponse.builder()
                .interviewSessionId(interviewSession.getId())
                .questionId(interviewQuestion.getId())
                .sseTicket(sseTicket)
                .createdAt(interviewSession.getCreatedAt())
                .build();
    }
}
