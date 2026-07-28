package com.example.sprout.domain.interview.dto.request;

import com.example.sprout.domain.interview.enums.InterviewSessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateInterviewSessionRequest(
        @NotNull
        InterviewSessionType type,
        @NotBlank @Size(min = 1, max = 20)
        String title,
        @NotEmpty
        List<Long> targetIds
) {
}
