package com.example.sprout.domain.interview.event;

import com.example.sprout.domain.interview.service.InterviewSseService;
import com.example.sprout.domain.interview.sse.enums.SseCloseReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InterviewCompletedEventListener {

    private final InterviewSseService interviewSseService;

    // 총평 생성 DB 커밋이 완료된 뒤에만 면접 세션 종료(SSE 연결 종료 + Redis 삭제)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(InterviewCompletedEvent event) {
        interviewSseService.closeSession(
                event.interviewSessionId(), SseCloseReason.INTERVIEW_COMPLETED
        );
    }

}
