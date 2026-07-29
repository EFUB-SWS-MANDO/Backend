package com.example.sprout.domain.interview.dto.request;

import com.example.sprout.domain.interview.enums.InterviewQuestionType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInterviewQuestionRequest(
        @NotNull
        InterviewQuestionType type,
        @NotNull @Positive
        Long questionId,
        @NotBlank
        String answer
) {
    @AssertTrue(message = "type은 FOLLOW_UP 또는 EXTRA만 가능합니다.")
    public boolean isValidType() {
        return type == InterviewQuestionType.FOLLOW_UP ||
                type == InterviewQuestionType.EXTRA;
    }
}
