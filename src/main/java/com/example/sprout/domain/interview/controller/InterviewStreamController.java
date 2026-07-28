package com.example.sprout.domain.interview.controller;

import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.interview.service.InterviewSseService;
import com.example.sprout.domain.interview.sse.ticket.SseTicketService;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewStreamController {

    private final SseTicketService sseTicketService;
    private final InterviewSseService interviewSseService;
    private final InterviewSessionRepository interviewSessionRepository;

    @GetMapping(value = "/{interviewSessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @PathVariable Long interviewSessionId,
            @RequestParam String ticket,
            @RequestHeader(value = "Last-Event-ID", required = false) Long lastEventIdHeader,
            @RequestParam(required = false) Long lastEventId
    ) {
        sseTicketService.verifyAndConsumeTicket(ticket, interviewSessionId);

        if (!interviewSessionRepository.existsById(interviewSessionId)) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_NOT_FOUND);
        }

        // 헤더(브라우저 자동 재연결) lastEventId 우선, 없을 시 쿼리 파라미터(프론트 수동 재연결) 사용
        Long effectiveLastEventId = (lastEventIdHeader != null)
                ? lastEventIdHeader
                : lastEventId;

        log.info("SSE 연결 시작 - sessionId={}, lastEventId={}", interviewSessionId, effectiveLastEventId);
        return interviewSseService.connect(interviewSessionId, effectiveLastEventId);
    }
}
