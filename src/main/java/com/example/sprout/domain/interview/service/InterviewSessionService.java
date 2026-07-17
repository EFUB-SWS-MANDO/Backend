package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.domain.interview.repository.InterviewAnswerRepository;
import com.example.sprout.domain.interview.repository.InterviewQuestionRepository;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewSessionService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;


    // 모의면접 단건 삭제
    @Transactional
    @CacheEvict(value = "interviewSessions", key = "'member:' + #requesterId")
    public void deleteInterview(Long requesterId, Long interviewSessionId) {
        InterviewSession interview = getInterviewSession(interviewSessionId);
        validateOwnership(requesterId, interview);
        deleteInterviewSession(interview);

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

}
