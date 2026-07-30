package com.example.sprout.domain.interview.dto.response;

import com.example.sprout.domain.interview.entity.InterviewQuestion;
import com.example.sprout.domain.interview.enums.InterviewQuestionType;
import lombok.Builder;

@Builder
public record InterviewQuestionDetailResponse(
        Long questionId,
        String question,
        String answer,
        InterviewQuestionType questionType
) {
    public static InterviewQuestionDetailResponse of(
            InterviewQuestion question, String answerContent
    ) {
        return InterviewQuestionDetailResponse.builder()
                .questionId(question.getId())
                .question(question.getContent())
                .answer(answerContent)
                .questionType(question.getType())
                .build();
    }
}