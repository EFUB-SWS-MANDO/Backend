package com.example.sprout.domain.interview.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.interview.dto.response.InterviewFeedbackResponse;
import com.example.sprout.domain.interview.dto.response.InterviewSessionCursorResponse;
import com.example.sprout.domain.interview.service.InterviewSessionService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/interviews")
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;


    @GetMapping
    public ResponseEntity<ApiResponse<InterviewSessionCursorResponse>> getInterviews(
            @AuthMember Long requesterId,
            @RequestParam(name = "idAfter", required = false) @Positive Long idAfter,
            @RequestParam(name = "limit", defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        log.info("모의면접 목록 조회 요청, requesterId={}", requesterId);

        InterviewSessionCursorResponse response = interviewSessionService.getInterviews(requesterId, idAfter, limit);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "모의면접 목록 조회 성공",
                        response
                )
        );
    }

    @GetMapping("/{interviewSessionId}/feedback")
    public ResponseEntity<ApiResponse<InterviewFeedbackResponse>> getInterviewFeedback(
            @AuthMember Long requesterId,
            @PathVariable(name = "interviewSessionId") Long interviewSessionId
    ) {
        log.info("모의면접 총평 조회 요청, requesterId={}, interviewSessionId={}", requesterId, interviewSessionId);

        InterviewFeedbackResponse response = interviewSessionService.getFeedback(requesterId, interviewSessionId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "모의면접 총평 조회 성공",
                        response
                )
        );
    }

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
