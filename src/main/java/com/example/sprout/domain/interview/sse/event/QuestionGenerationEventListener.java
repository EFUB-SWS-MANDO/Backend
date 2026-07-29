package com.example.sprout.domain.interview.sse.event;

import com.example.sprout.domain.interview.service.InterviewSessionQuestionGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class QuestionGenerationEventListener {

    private final InterviewSessionQuestionGenerationService questionGenerationService;

    // 원본 트랜잭션이 실제로 커밋된 이후 실행
    //
    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(QuestionGenerationRequestedEvent event) {
        questionGenerationService.generateQuestion(
                event.sessionId(), event.questionId(), event.type(), event.targetIds()
        );
    }

}
