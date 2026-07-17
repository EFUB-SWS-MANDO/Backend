package com.example.sprout.domain.resume.dto.response;

public record ResumeDetailItem (
        Long questionId,
        Long order,
        String content,
        String answer,
        String description
){}