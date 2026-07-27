package com.example.sprout.domain.dashboard.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.dashboard.dto.DashboardResponse;
import com.example.sprout.domain.dashboard.service.DashboardService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(@AuthMember Long requesterId) {
        log.info("Dashboard 조회 요청 - requesterId:{}", requesterId);

        DashboardResponse response = dashboardService.getDashboard(requesterId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "대시보드 조회 성공",
                        response
                )
        );
    }

}
