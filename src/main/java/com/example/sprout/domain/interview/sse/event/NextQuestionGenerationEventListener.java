package com.example.sprout.domain.interview.sse.event;

import com.example.sprout.domain.interview.service.InterviewSessionQuestionGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NextQuestionGenerationEventListener {

    private final InterviewSessionQuestionGenerationService questionGenerationService;

    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NextQuestionGenerationRequestedEvent event) {
        questionGenerationService.generateNextQuestion(
                event.sessionId(), event.questionId(), event.questionType(), event.lastAnswer()
        );
    }
}
