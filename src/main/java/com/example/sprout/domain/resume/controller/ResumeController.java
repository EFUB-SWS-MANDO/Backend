package com.example.sprout.domain.resume.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.resume.service.ResumeService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    // 자소서 삭제
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(@AuthMember Long requesterId,
                                                    @PathVariable Long resumeId) {
        log.info("자소서 삭제 API 호출 - resumeId: {}", resumeId);

        resumeService.deleteResume(requesterId, resumeId);
        return ResponseEntity.ok().body(ApiResponse.success("자소서 삭제 성공"));
    }
}
