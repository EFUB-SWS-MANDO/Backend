package com.example.sprout.domain.dashboard.dto;

import com.example.sprout.domain.dashboard.enums.ActivityType;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.resume.entity.Resume;

import java.time.LocalDateTime;

public record RecentActivityResponse(
        ActivityType type,
        String title,
        LocalDateTime updatedAt
) {
    public static RecentActivityResponse of(ActivityType type, Post post) {
        return new RecentActivityResponse(
                type, post.getTitle(), post.getUpdatedAt()
        );
    }

    public static RecentActivityResponse of(ActivityType type, Resume resume) {
        return new RecentActivityResponse(
                type, resume.getTitle(), resume.getUpdatedAt()
        );
    }

    public static RecentActivityResponse of(ActivityType type, InterviewSession interviewSession) {
        return new RecentActivityResponse(
                type, interviewSession.getTitle(), interviewSession.getUpdatedAt()
        );
    }
}
