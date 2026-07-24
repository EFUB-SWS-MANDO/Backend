package com.example.sprout.domain.interview.dto.response;

import com.example.sprout.domain.interview.entity.InterviewSession;

import java.util.List;

public record InterviewSessionCursorResponse(
        List<InterviewSessionSummaryResponse> interviews,
        Long nextIdAfter,
        boolean hasNext,
        long totalElements
) {
    public static InterviewSessionCursorResponse of(
            List<InterviewSession> interviewSessions,
            Long nextIdAfter,
            boolean hasNext,
            long totalElements
    ) {
        return new InterviewSessionCursorResponse(
                interviewSessions.stream()
                        .map(InterviewSessionSummaryResponse::of)
                        .toList(),
                nextIdAfter,
                hasNext,
                totalElements
        );
    }
}
