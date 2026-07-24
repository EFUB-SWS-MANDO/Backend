package com.example.sprout.domain.resume.dto.response;

import com.example.sprout.domain.resume.entity.Resume;

import java.time.LocalDateTime;

public record ResumeListItem(
        Long resumeId,
        String title,
        LocalDateTime createdAt
) {
    public static ResumeListItem from(Resume resume) {
        return new ResumeListItem(
                resume.getId(),
                resume.getTitle(),
                resume.getCreatedAt()
        );
    }
}
