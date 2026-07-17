package com.example.sprout.domain.resume.dto.response;

import java.time.LocalDateTime;

public record ResumeListItem(
        Long resumeId,
        String title,
        LocalDateTime createdAt
) {
}
