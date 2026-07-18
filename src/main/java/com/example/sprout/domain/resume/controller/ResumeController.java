package com.example.sprout.domain.resume.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.resume.dto.response.ResumeResponse;
import com.example.sprout.domain.resume.service.ResumeService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    // 자소서 상세 조회
    @GetMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResumeDetail(@AuthMember Long requesterId,
                                                                       @PathVariable("resumeId") Long resumeId) {
        log.info("자소서 상세 조회 API 호출 - resumeId: {}", resumeId);

        ResumeResponse response = resumeService.getResumeDetail(requesterId, resumeId);
        return ResponseEntity.ok().body(ApiResponse.success("자소서 상세 조회 성공", response));
    }
}
