package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.dto.request.CreateInterviewFeedbackRequest;
import com.example.sprout.domain.interview.dto.request.CreateInterviewQuestionRequest;
import com.example.sprout.domain.interview.dto.request.CreateInterviewSessionRequest;
import com.example.sprout.domain.interview.dto.response.*;
import com.example.sprout.domain.interview.entity.InterviewAnswer;
import com.example.sprout.domain.interview.entity.InterviewQuestion;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.enums.InterviewQuestionType;
import com.example.sprout.domain.interview.enums.InterviewSessionStatus;
import com.example.sprout.domain.interview.event.InterviewCompletedEvent;
import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.domain.interview.repository.InterviewAnswerRepository;
import com.example.sprout.domain.interview.repository.InterviewQuestionRepository;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.interview.sse.event.NextQuestionGenerationRequestedEvent;
import com.example.sprout.domain.interview.sse.event.QuestionGenerationRequestedEvent;
import com.example.sprout.domain.interview.sse.ticket.SseTicketService;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.global.common.util.CursorPageUtils;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewSessionService {

    private final SseTicketService sseTicketService;
    private final InterviewSessionQuestionGenerationService questionGenerationService;

    private final ApplicationEventPublisher eventPublisher;

    private final MemberRepository memberRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;


    // 모의면접 생성
    @Transactional
    public InterviewSessionResponse createInterview(Long requesterId, CreateInterviewSessionRequest request) {

        Member requester = memberRepository.getReferenceById(requesterId);

        InterviewSession interviewSession = InterviewSession.builder()
                .type(request.type()).member(requester).title(request.title()).build();
        InterviewQuestion interviewQuestion = InterviewQuestion.builder()
                .type(InterviewQuestionType.INITIAL).session(interviewSession).build();

        interviewSessionRepository.save(interviewSession);
        interviewQuestionRepository.save(interviewQuestion);

        // SSE 단발성 인증 티켓 발급
        String sseTicket = sseTicketService.issueTicket(interviewSession.getId());

        // 백그라운드 AI 첫 질문 생성 (생성 트랜잭션 완료 이후 실행)
        eventPublisher.publishEvent(new QuestionGenerationRequestedEvent(
                interviewSession.getId(), interviewQuestion.getId(),
                request.type(), request.targetIds()
        ));

        log.info("모의면접 생성 완료 - sessionId={}, questionId={}", interviewSession.getId(), interviewQuestion.getId());

        return InterviewSessionResponse.from(interviewSession, interviewQuestion, sseTicket);
    }

    // 모의면접 답변 제출 및 다음 질문 생성
    @Transactional
    public SubmitInterviewAnswerResponse createQuestion(
            Long requesterId, Long interviewSessionId, CreateInterviewQuestionRequest request
    ) {

        InterviewSession interviewSession = getInterviewSession(interviewSessionId);
        validateOwnership(requesterId, interviewSession);

        InterviewQuestion currentQuestion = getInterviewQuestion(request.questionId());
        validateQuestionBelongsToSession(currentQuestion, interviewSession);
        validateAnswerNotSubmitted(currentQuestion);

        InterviewAnswer answer = InterviewAnswer.builder()
                .session(interviewSession)
                .question(currentQuestion)
                .content(request.answer())
                .build();
        interviewAnswerRepository.save(answer);

        InterviewQuestion nextQuestion = InterviewQuestion.builder()
                .type(request.type())
                .session(interviewSession)
                .build();
        interviewQuestionRepository.save(nextQuestion);

        // 트랜잭션 커밋 이후 다음 질문 생성
        eventPublisher.publishEvent(new NextQuestionGenerationRequestedEvent(
                interviewSessionId, nextQuestion.getId(), request.type(), request.answer()
        ));

        log.info("모의면접 답변 제출 및 다음 질문 생성 요청 완료 - sessionId={}, questionId={}, nextQuestionId={}",
                interviewSessionId, currentQuestion.getId(), nextQuestion.getId());
        return SubmitInterviewAnswerResponse.of(nextQuestion);
    }

    // 모의면접 상세 조회
    @Cacheable(
            value = "interviewDetail",
            key = "#interviewSessionId",
            unless = "#result.status().name() != 'COMPLETED'" // 완료된 면접일 경우에만 캐시
    )
    public InterviewSessionDetailResponse getInterview(
            Long requesterId, Long interviewSessionId
    ) {
        InterviewSession interviewSession = getInterviewSession(interviewSessionId);
        validateOwnership(requesterId, interviewSession);

        String sseTicket = (interviewSession.getStatus() == InterviewSessionStatus.IN_PROGRESS)
                ? sseTicketService.issueTicket(interviewSession.getId())
                : null;

        log.info("[Cache Miss] 모의면접 상세 조회 완료 - requesterId={}, sessionId={}", requesterId, interviewSessionId);

        return toInterviewSessionDetailResponse(interviewSession, sseTicket);
    }

    // 모의면접 목록 조회
    // 목록 진입 시 가장 빈번히 조회되는 첫 페이지만 캐싱,
    // cursor 변경 및 limit 변경 요청은 캐시 대상에서 제외 (DB 직접 조회)
    @Cacheable(
            value = "interviewSessions",
            key = "'member:' + #requesterId",
            condition = "#idAfter == null && #limit == 10"
    )
    public InterviewSessionCursorResponse getInterviews(Long requesterId, Long idAfter, int limit) {
        List<InterviewSession> interviewSessions = interviewSessionRepository
                .findCursorPageByMemberId(requesterId, idAfter, Pageable.ofSize(limit + 1));

        boolean hasNext = CursorPageUtils.hasNextPage(interviewSessions, limit);
        interviewSessions = CursorPageUtils.trimToPageSize(interviewSessions, limit, hasNext);
        Long nextIdAfter = CursorPageUtils.resolveNextIdAfter(interviewSessions, hasNext, InterviewSession::getId);

        long totalElements = interviewSessionRepository.countAllByMemberId(requesterId);

        log.info("[Cache Miss] 모의면접 목록 조회 완료 - requesterId={}", requesterId);
        return InterviewSessionCursorResponse.of(
                interviewSessions,
                nextIdAfter,
                hasNext,
                totalElements
        );
    }

    // 모의면접 마지막 답변 제출 및 총평 생성
    @Transactional
    public InterviewFeedbackResponse createFeedback(
            Long requesterId, Long interviewSessionId, CreateInterviewFeedbackRequest request
    ){
        InterviewSession interviewSession = getInterviewSession(interviewSessionId);
        validateOwnership(requesterId, interviewSession);
        validateInProgress(interviewSession);

        InterviewQuestion interviewQuestion = getInterviewQuestion(request.questionId());
        validateQuestionBelongsToSession(interviewQuestion, interviewSession);
        validateAnswerNotSubmitted(interviewQuestion);

        InterviewAnswer interviewAnswer = InterviewAnswer.builder()
                .session(interviewSession)
                .question(interviewQuestion)
                .content(request.answer())
                .build();
        interviewAnswerRepository.save(interviewAnswer);

        InterviewFeedbackResult result = questionGenerationService.generateFeedback(interviewSession);

        interviewSession.complete();
        interviewSession.recordFeedbackResult(result.feedbackSummary(), result.feedback());

        eventPublisher.publishEvent(new InterviewCompletedEvent(interviewSessionId));

        log.info("모의면접 총평 생성 완료 - requesterId={}, interviewSessionId={}", requesterId, interviewSessionId);
        return InterviewFeedbackResponse.from(interviewSession);
    }

    // 모의면접 총평 조회
    public InterviewFeedbackResponse getFeedback(Long requesterId, Long interviewSessionId) {
        InterviewSession interviewSession = getInterviewSession(interviewSessionId);
        validateOwnership(requesterId, interviewSession);
        validateFeedbackNotFound(interviewSession);

        log.info("모의면접 총평 조회 완료 - requesterId={}, interviewSessionId={}", requesterId, interviewSessionId);

        return InterviewFeedbackResponse.from(interviewSession);
    }

    // 모의면접 단건 삭제
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "interviewSessions", key = "'member:' + #requesterId"),
            @CacheEvict(value = "interviewDetail", key = "#interviewSessionId")
    })
    public void deleteInterview(Long requesterId, Long interviewSessionId) {
        InterviewSession interviewSession = getInterviewSession(interviewSessionId);
        validateOwnership(requesterId, interviewSession);
        deleteInterviewSession(interviewSession);

        log.info("모의면접 삭제 완료 - requesterId={}, interviewSessionId={}", requesterId, interviewSessionId);
    }


    // 회원 모의면접 일괄 삭제
    @Transactional
    @CacheEvict(value = "interviewSessions", key = "'member:' + #member.id")
    public void deleteAllByMember(Member member) {
        int deletedAnswers = interviewAnswerRepository.deleteAllByMember(member);
        int deletedQuestions = interviewQuestionRepository.deleteAllByMember(member);
        int deletedSessions = interviewSessionRepository.deleteAllByMember(member);

        log.debug(
                "회원({}) 면접 데이터 일괄 삭제 완료 - interviewSessions={}, interviewQuestions={}, interviewAnswers={}",
                member.getId(), deletedSessions, deletedQuestions, deletedAnswers
        );
    }

    // === Helper Method ===
    private InterviewSession getInterviewSession(Long interviewSessionId) {
        return interviewSessionRepository.findById(interviewSessionId)
                .orElseThrow(() -> new BusinessException(InterviewErrorCode.INTERVIEW_NOT_FOUND));
    }

    private InterviewQuestion getInterviewQuestion(Long interviewQuestionId) {
        return interviewQuestionRepository.findById(interviewQuestionId)
                .orElseThrow(() -> new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND));
    }

    // 요청자 == 모의면접 세션 주인 검증
    private void validateOwnership(Long requesterId, InterviewSession interviewSession) {
        if(!requesterId.equals(interviewSession.getMember().getId())) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_ACCESS_DENIED);
        }
    }

    // 모의면접 세션 단건 삭제 (answer -> question -> session 순서로 삭제)
    private void deleteInterviewSession(InterviewSession interviewSession) {
        int deletedAnswers = interviewAnswerRepository.deleteAllBySession(interviewSession);
        int deletedQuestions = interviewQuestionRepository.deleteAllBySession(interviewSession);

        interviewSessionRepository.delete(interviewSession);

        log.debug(
                "면접 세션({}) 데이터 삭제 완료 - interviewQuestions={}, interviewAnswers={}",
                interviewSession.getId(), deletedQuestions, deletedAnswers
        );
    }

    // 총평이 없으면 404
    private void validateFeedbackNotFound(InterviewSession interviewSession) {
        if(!interviewSession.hasFeedback()) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_FEEDBACK_NOT_FOUND);
        }
    }

    // 세션이 진행 중(IN_PROGRESS)이 아니면 409
    private void validateInProgress(InterviewSession interviewSession) {
        if (interviewSession.getStatus() != InterviewSessionStatus.IN_PROGRESS) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_ALREADY_COMPLETED);
        }
    }

    // 질문이 해당 세션 소속인지 검증
    private void validateQuestionBelongsToSession(
            InterviewQuestion interviewQuestion, InterviewSession interviewSession
    ) {
        if (!interviewQuestion.getSession().getId().equals(interviewSession.getId())) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }
    }

    // 이미 답변이 제출된 질문인지 검증
    private void validateAnswerNotSubmitted(InterviewQuestion interviewQuestion) {
        if (interviewAnswerRepository.existsByQuestion(interviewQuestion)) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_ANSWER_ALREADY_EXISTS);
        }
    }

    // DTO 변환
    private InterviewSessionDetailResponse toInterviewSessionDetailResponse(
            InterviewSession interviewSession, String sseTicket
    ){
        List<InterviewQuestion> questions = interviewQuestionRepository.findAllBySessionOrderByIdAsc(interviewSession);

        // Map<Question id, InterviewAnswer content>
        Map<Long, String> answerMap = interviewAnswerRepository
                .findAllBySession(interviewSession).stream()
                .collect(Collectors.toMap(
                        i -> i.getQuestion().getId(),
                        i -> i.getContent()
                ));

        List<InterviewQuestionDetailResponse> questionDetailResponses
                = questions.stream()
                .map(q -> InterviewQuestionDetailResponse.of(
                        q, answerMap.getOrDefault(q.getId(), null)
                ))
                .toList();

        return InterviewSessionDetailResponse.of(
                interviewSession, questionDetailResponses, sseTicket
        );
    }

}
