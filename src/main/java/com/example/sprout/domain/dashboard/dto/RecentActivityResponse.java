package com.example.sprout.domain.dashboard.dto;

import com.example.sprout.domain.dashboard.enums.ActivityType;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.resume.entity.Resume;

import java.time.LocalDateTime;

public record RecentActivityResponse(
        Long id,
        ActivityType type,
        String title,
        LocalDateTime updatedAt
) {
    public static RecentActivityResponse of(ActivityType type, Post post) {
        return new RecentActivityResponse(
                post.getId(), type, post.getTitle(), post.getUpdatedAt()
        );
    }

    public static RecentActivityResponse of(ActivityType type, Resume resume) {
        return new RecentActivityResponse(
                resume.getId(), type, resume.getTitle(), resume.getUpdatedAt()
        );
    }

    public static RecentActivityResponse of(ActivityType type, InterviewSession interviewSession) {
        return new RecentActivityResponse(
                interviewSession.getId(), type, interviewSession.getTitle(), interviewSession.getUpdatedAt()
        );
    }
}
