package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.entity.InterviewQuestion;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.enums.InterviewSessionType;
import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.domain.interview.repository.InterviewQuestionRepository;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.interview.sse.connection.SseSubscriptionRegistry;
import com.example.sprout.domain.interview.sse.event.payload.QuestionDeltaEvent;
import com.example.sprout.domain.interview.sse.event.payload.QuestionDoneEvent;
import com.example.sprout.domain.interview.sse.event.payload.QuestionErrorEvent;
import com.example.sprout.domain.post.repository.PostCategoryRepository;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import com.example.sprout.global.ai.client.AiChatClient;
import com.example.sprout.global.ai.dto.AiChatRequest;
import com.example.sprout.global.ai.dto.AiMessage;
import com.example.sprout.global.ai.prompt.PromptTemplateLoader;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Disposable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionQuestionGenerationService {

    private final AiChatClient aiChatClient;
    private final PromptTemplateLoader promptTemplateLoader;
    private final SseSubscriptionRegistry sseRegistry;
    private final InterviewSseService interviewSseService;

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final ResumeRepository resumeRepository;

    @Transactional
    public void generateQuestion(
            Long sessionId, Long questionId, InterviewSessionType type, List<Long> targetIds
    ) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(InterviewErrorCode.INTERVIEW_NOT_FOUND));

        String summary = session.getSummary();

        // 최초 질문 생성 시 (요약 없음) 요약 생성해 세션에 저장
        if (summary == null) {
            String referenceContent = collectReferenceContent(type, targetIds);
            summary = summarize(referenceContent);
            session.recordSummary(summary);
        }

        AiChatRequest request = buildInitialQuestionRequest(summary);
        streamQuestion(sessionId, questionId, request);
    }

    private String collectReferenceContent(InterviewSessionType type, List<Long> targetIds) {
        return switch (type) {
            case POST -> postRepository.findAllById(targetIds).stream()
                    .map(p -> "제목: %s\n내용: %s".formatted(p.getTitle(), p.getContent()))
                    .collect(Collectors.joining("\n\n---\n\n"));

            case CATEGORY -> postCategoryRepository.findAllWithPostAndCategoryByCategoryIdIn(targetIds).stream()
                    .map(pc -> "[카테고리: %s]\n제목: %s\n내용: %s"
                            .formatted(pc.getCategory().getType(), pc.getPost().getTitle(), pc.getPost().getContent()))
                    .collect(Collectors.joining("\n\n---\n\n"));

            case RESUME -> resumeRepository.findAllWithDraftsByIdIn(targetIds).stream()
                    .map(this::formatResume)
                    .collect(Collectors.joining("\n\n===\n\n"));
        };
    }

    private String formatResume(Resume resume) {
        String drafts = resume.getResumeDraftList().stream()
                .map(d -> "Q: %s\nA: %s".formatted(d.getQuestion(), d.getAnswer()))
                .collect(Collectors.joining("\n\n"));
        return "[자기소개서: %s]\n%s".formatted(resume.getTitle(), drafts);
    }

    private String summarize(String referenceContent) {
        PromptTemplateLoader.PromptTemplate template = promptTemplateLoader.load("interview-summary.txt");
        String systemPrompt = template.render(Map.of("content", referenceContent));

        AiChatRequest request = AiChatRequest.builder()
                .messages(List.of(new AiMessage("system", systemPrompt)))
                .temperature(0.3)
                .build();

        return aiChatClient.chat(request).content();
    }

    private AiChatRequest buildInitialQuestionRequest(String summary) {
        PromptTemplateLoader.PromptTemplate template = promptTemplateLoader.load("interview-question-initial.txt");
        String systemPrompt = template.render(Map.of("summary", summary));

        return AiChatRequest.builder()
                .messages(List.of(new AiMessage("system", systemPrompt)))
                .temperature(0.7)
                .build();
    }

    private void streamQuestion(Long sessionId, Long questionId, AiChatRequest request) {
        StringBuilder accumulated = new StringBuilder();

        Disposable subscription = aiChatClient.chatStream(request)
                .doOnNext(accumulated::append)
                .subscribe(
                        delta -> interviewSseService.publish(sessionId, "question-delta",
                                new QuestionDeltaEvent(questionId, delta)),
                        error -> {
                            log.error("면접 질문 생성 실패 - sessionId={}, questionId={}", sessionId, questionId);
                            interviewSseService.publish(sessionId, "question-error",
                                    new QuestionErrorEvent(questionId, "질문 생성에 실패했습니다."));
                        },
                        () -> {
                            InterviewQuestion question = interviewQuestionRepository.findById(questionId)
                                    .orElseThrow(() -> new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND));
                            question.recordContent(accumulated.toString());
                            interviewQuestionRepository.save(question);
                            interviewSseService.publish(sessionId, "question-done",
                                    new QuestionDoneEvent(questionId, accumulated.toString()));
                        }
                );

        sseRegistry.register(sessionId, subscription);
    }
}
