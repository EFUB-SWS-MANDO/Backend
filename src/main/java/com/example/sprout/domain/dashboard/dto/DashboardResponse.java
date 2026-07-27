package com.example.sprout.domain.dashboard.dto;

import com.example.sprout.domain.motivation.entity.Motivation;

import java.util.List;

public record DashboardResponse(
        String motivation,
        DashboardStatisticsResponse statistics,
        List<RecentActivityResponse> recentActivities
){
    public static DashboardResponse of(
            Motivation motivation, DashboardStatisticsResponse statistics, List<RecentActivityResponse> recentActivities
    ){
        return new DashboardResponse(
                motivation.getContent(),
                statistics,
                recentActivities
        );
    }
}
