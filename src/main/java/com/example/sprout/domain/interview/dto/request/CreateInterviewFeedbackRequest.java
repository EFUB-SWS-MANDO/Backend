package com.example.sprout.domain.interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInterviewFeedbackRequest(
        @NotNull @Positive
        Long questionId,
        @NotBlank
        String answer
) {
}
