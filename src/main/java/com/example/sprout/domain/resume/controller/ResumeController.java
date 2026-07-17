package com.example.sprout.domain.resume.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.resume.dto.request.CreateResumeRequest;
import com.example.sprout.domain.resume.dto.request.GetResumeListCondition;
import com.example.sprout.domain.resume.dto.response.GetResumeListResponse;
import com.example.sprout.domain.resume.dto.response.ResumeResponse;
import com.example.sprout.domain.resume.service.ResumeService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    // 자소서 생성
    @PostMapping
    public ResponseEntity<ApiResponse<ResumeResponse>> createResume(@AuthMember Long requesterId,
                                                                    @RequestBody CreateResumeRequest request) {
        log.info("자소서 생성 API 호출 - requesterId: {}", requesterId);

        ResumeResponse response = resumeService.createResume(requesterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("자소서 생성 성공", response));
    }

    // 자소서 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<GetResumeListResponse>> getResumeList(@AuthMember Long requesterId,
                                                                            @ModelAttribute GetResumeListCondition condition) {
        log.info("자소서 목록 조회 API 호출 - requesterId: {}", requesterId);

        GetResumeListResponse response = resumeService.getResumeList(requesterId, condition.idAfter(), condition.limit(), condition.keyword());
        return ResponseEntity.ok().body(ApiResponse.success("자소서 목록 조회 성공", response));
    }
}
