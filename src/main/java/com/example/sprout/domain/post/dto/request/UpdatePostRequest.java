package com.example.sprout.domain.post.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdatePostRequest(
        @Size(min=1, max=20)
        String title,
        String content,
        @NotNull
        List<String> categories
) {}
