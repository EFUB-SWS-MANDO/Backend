package com.example.sprout.domain.interview.dto;

import com.example.sprout.domain.interview.entity.InterviewSession;

public record InterviewFeedbackResponse(
        String feedbackSummary,
        String feedback
) {
    public static InterviewFeedbackResponse from(InterviewSession interviewSession) {
        return new InterviewFeedbackResponse(
                interviewSession.getFeedbackSummary(),
                interviewSession.getFeedback()
        );
    }
}
