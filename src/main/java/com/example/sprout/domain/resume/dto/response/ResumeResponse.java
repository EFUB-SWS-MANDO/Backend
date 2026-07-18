package com.example.sprout.domain.resume.dto.response;

import com.example.sprout.domain.resume.entity.Resume;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeResponse (
        Long resumeId,
        String title,
        LocalDateTime createdAt,
        List<ResumeDetailItem> questions
) {
    public static ResumeResponse of(Resume resume, List<ResumeDetailItem> resumeDetailItemList) {
        return new ResumeResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getCreatedAt(),
                resumeDetailItemList
        );
    }
}