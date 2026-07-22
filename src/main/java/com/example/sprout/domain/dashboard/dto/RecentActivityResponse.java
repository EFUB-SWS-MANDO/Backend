package com.example.sprout.domain.dashboard.dto;

import com.example.sprout.domain.dashboard.enums.ActivityType;

import java.time.LocalDateTime;

public record RecentActivityResponse(
        ActivityType type,
        String title,
        LocalDateTime updatedAt
) {
}
