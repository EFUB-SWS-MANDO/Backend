package com.example.sprout.domain.resume.dto.request;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.resume.entity.Resume;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateResumeRequest(
        @NotBlank
        String title,
        @NotNull
        List<Long> postIds,
        @NotNull
        List<QuestionDto> questions

) {
    public record QuestionDto (
            @NotNull
            @Positive
            Long order,
            @NotNull
            String content
    ) {}

    public Resume toEntity (Member author) {
        return Resume.builder()
                .title(title)
                .author(author)
                .build();
    }
}
