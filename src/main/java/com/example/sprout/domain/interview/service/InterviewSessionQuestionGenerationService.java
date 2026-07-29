package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.entity.InterviewAnswer;
import com.example.sprout.domain.interview.entity.InterviewQuestion;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.enums.InterviewQuestionType;
import com.example.sprout.domain.interview.enums.InterviewSessionType;
import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.domain.interview.repository.InterviewAnswerRepository;
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
import com.example.sprout.global.error.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

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

    @Transactional
    public void generateNextQuestion(
            Long sessionId, Long questionId, InterviewQuestionType questionType, String lastAnswer
    ) {
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(InterviewErrorCode.INTERVIEW_NOT_FOUND));

        String qnaHistory = formatQnaHistory(sessionId);

        AiChatRequest request = switch (questionType) {
            case FOLLOW_UP -> buildFollowUpRequest(session.getSummary(), qnaHistory, lastAnswer);
            case EXTRA -> buildExtraRequest(session.getSummary(), qnaHistory);
            case INITIAL -> throw new IllegalArgumentException();
        };

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

    private String formatQnaHistory(Long sessionId) {
        return interviewQuestionRepository.findQnaHistoryBySessionId(sessionId).stream()
                .filter(r -> r.question() != null)
                .map(r -> {
                    String answer = (r.answer() != null)? r.answer() : "(미답변)";
                    return "Q: %s\nA: %s".formatted(r.question(), answer);
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String summarize(String referenceContent) {
        PromptTemplateLoader.PromptTemplate template = promptTemplateLoader.load("interview-summary.txt");
        String systemPrompt = template.render(Map.of("content", referenceContent));

        AiChatRequest request = AiChatRequest.builder()
                .messages(List.of(new AiMessage("system", systemPrompt)))
                .build();

        return aiChatClient.chat(request).content();
    }

    // 첫 질문 생성 요청 프롬프트 생성
    private AiChatRequest buildInitialQuestionRequest(String summary) {
        PromptTemplateLoader.PromptTemplate template = promptTemplateLoader.load("interview-question-initial.txt");
        String systemPrompt = template.render(Map.of("summary", summary));

        return AiChatRequest.builder()
                .messages(List.of(new AiMessage("system", systemPrompt)))
                .build();
    }

    // 꼬리 질문 생성 요청 프롬프트 생성
    private AiChatRequest buildFollowUpRequest(String summary, String qnaHistory, String lastAnswer) {
        PromptTemplateLoader.PromptTemplate template = promptTemplateLoader.load("interview-question-follow-up.txt");
        String systemPrompt = template.render(Map.of(
                "summary", summary,
                "qna_history", qnaHistory,
                "last_answer", lastAnswer
        ));

        return AiChatRequest.builder()
                .messages(List.of(new AiMessage("system", systemPrompt)))
                .build();
    }

    // 추가 질문 생성 요청 프롬프트 생성
    private AiChatRequest buildExtraRequest(String summary, String qnaHistory) {
        PromptTemplateLoader.PromptTemplate template = promptTemplateLoader.load("interview-question-extra.txt");
        String systemPrompt = template.render(Map.of(
                "summary", summary,
                "qna_history", qnaHistory
        ));

        return AiChatRequest.builder()
                .messages(List.of(new AiMessage("system", systemPrompt)))
                .build();
    }

    private void streamQuestion(Long sessionId, Long questionId, AiChatRequest request) {
        // AI 스트리밍 응답을 누적 저장하기 위한 버퍼
        StringBuilder accumulated = new StringBuilder();

        // AI 응답 스트림 구독
        // SSE 연결 종료 시 스트림을 취소하기 위해 관리
        Disposable subscription = aiChatClient.chatStream(request)
                // 각 응답 조각 누적
                .doOnNext(accumulated::append)
                // AI 응답 처리 작업을 별도 스레드에서 수행
                // SSE 전송이나 DB 저장 같은 후속 작업이 AI 스트림 처리 스레드를 막지 않도록 분리
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        // 응답 조각 도착할 때마다 클라이언트로 실시간 전송
                        delta -> interviewSseService.publish(sessionId, "question-delta",
                                new QuestionDeltaEvent(questionId, delta)),
                        // AI 스트림 처리 중 오류 발생 시 실패 이벤트 전송
                        error -> {
                            log.error("면접 질문 생성 실패 - sessionId={}, questionId={}", sessionId, questionId);
                            interviewSseService.publish(sessionId, "question-error",
                                    new QuestionErrorEvent(questionId, "질문 생성에 실패했습니다."));
                        },
                        // AI 스트림이 정상적으로 완료된 경우
                        // 누적된 전체 질문 내용 DB 저장 후 완료 이벤트 전송
                        () -> {
                            InterviewQuestion question = interviewQuestionRepository.findById(questionId)
                                    .orElseThrow(() -> new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND));
                            question.recordContent(accumulated.toString());
                            interviewQuestionRepository.save(question);
                            interviewSseService.publish(sessionId, "question-done",
                                    new QuestionDoneEvent(questionId, accumulated.toString()));
                        }
                );

        // 세션별 AI 스트리밍 구독 관리
        // SSE 연결 종료 시 해당 AI 스트림도 함께 취소할 수 있도록 등록
        sseRegistry.register(sessionId, subscription);
    }

}
