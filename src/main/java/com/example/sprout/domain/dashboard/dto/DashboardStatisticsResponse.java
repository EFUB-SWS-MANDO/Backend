package com.example.sprout.domain.dashboard.dto;

public record DashboardStatisticsResponse(
        int postCount,
        long receivedLikeCount,
        int attendanceStreak,
        int interviewCount,
        long resumeCount
) {
    public static DashboardStatisticsResponse of(
            int postCount, long receivedLikeCount, int attendanceStreak, int interviewCount, int resumeCount
    ){
        return new DashboardStatisticsResponse(
                postCount, receivedLikeCount, attendanceStreak, interviewCount, resumeCount
        );
    }
}
