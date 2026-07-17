package com.example.sprout.domain.resume.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeResponse (
        Long resumeId,
        String title,
        LocalDateTime createdAt,
        List<ResumeDetailItem> questions
) {}
