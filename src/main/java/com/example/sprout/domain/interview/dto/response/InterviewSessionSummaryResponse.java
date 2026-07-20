package com.example.sprout.domain.interview.dto.response;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.enums.InterviewSessionStatus;
import com.example.sprout.domain.interview.enums.InterviewSessionType;

import java.time.LocalDateTime;

public record InterviewSessionSummaryResponse(
        Long interviewSessionId,
        String title,
        InterviewSessionType type,
        InterviewSessionStatus status,
        LocalDateTime updatedAt
) {
    public static InterviewSessionSummaryResponse of(InterviewSession interviewSession) {
        return new InterviewSessionSummaryResponse(
                interviewSession.getId(),
                interviewSession.getTitle(),
                interviewSession.getType(),
                interviewSession.getStatus(),
                interviewSession.getUpdatedAt()
        );
    }
}
