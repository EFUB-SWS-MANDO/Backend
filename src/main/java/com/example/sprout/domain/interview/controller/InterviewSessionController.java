package com.example.sprout.domain.interview.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.interview.service.InterviewSessionService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/interviews")
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

    @DeleteMapping("/{interviewSessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteInterviewSession(
            @AuthMember Long requesterId,
            @PathVariable(name = "interviewSessionId") Long interviewSessionId) {
        log.info("모의면접 삭제 요청, requesterId={}", requesterId);

        interviewSessionService.deleteInterview(requesterId, interviewSessionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "모의면접 삭제 성공"
                )
        );
    }

}
