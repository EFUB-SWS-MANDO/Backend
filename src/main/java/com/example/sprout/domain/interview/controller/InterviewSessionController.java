package com.example.sprout.domain.interview.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.interview.dto.InterviewFeedbackResponse;
import com.example.sprout.domain.interview.service.InterviewSessionService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/interviews")
public class InterviewSessionController {

    private final InterviewSessionService interviewSessionService;

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
