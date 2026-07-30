package com.example.sprout.domain.interview.dto.response;

import com.example.sprout.domain.interview.entity.InterviewQuestion;

public record SubmitInterviewAnswerResponse(
        Long nextQuestionId
) {
    public static SubmitInterviewAnswerResponse of(InterviewQuestion question) {
        return new SubmitInterviewAnswerResponse(question.getId());
    }
}
