package com.example.sprout.domain.interview.dto.response;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.enums.InterviewQuestionType;
import com.example.sprout.domain.interview.enums.InterviewSessionStatus;
import com.example.sprout.domain.interview.enums.InterviewSessionType;
import lombok.Builder;

import java.util.List;

@Builder
public record InterviewSessionDetailResponse(
        Long interviewSessionId,
        String title,
        InterviewSessionType type,
        InterviewSessionStatus status,
        String sseTicket,
        List<InterviewQuestionDetailResponse> questions
) {
    public static InterviewSessionDetailResponse of(
            InterviewSession interviewSession,
            List<InterviewQuestionDetailResponse> questions,
            String sseTicket
    ) {
       return InterviewSessionDetailResponse.builder()
                .interviewSessionId(interviewSession.getId())
                .title(interviewSession.getTitle())
                .type(interviewSession.getType())
                .status(interviewSession.getStatus())
                .sseTicket(sseTicket)
                .questions(questions)
                .build();
    }

}
