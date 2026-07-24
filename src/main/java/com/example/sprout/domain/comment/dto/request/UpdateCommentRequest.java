package com.example.sprout.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 300, message = "댓글은 300자를 초과할 수 없습니다.")
        String content,

        @NotNull
        boolean isPrivate
) {}
