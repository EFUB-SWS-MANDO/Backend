package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewSessionService {

    private final InterviewSessionRepository interviewSessionRepository;

    private final InterviewQuestionService interviewQuestionService;
    private final InterviewAnswerService interviewAnswerService;

    @Transactional
    public void deleteAllByMember(Member member) {
        List<InterviewSession> interviewSessionList = interviewSessionRepository.findAllByMember(member);
        for (InterviewSession interviewSession : interviewSessionList) deleteInterviewSession(interviewSession);
    }

    private void deleteInterviewSession(InterviewSession interviewSession) {
        //InterviewSession 내 answer -> question 순서대로 우선 삭제
        interviewAnswerService.deleteAllByInterviewSession(interviewSession);
        interviewQuestionService.deleteAllByInterviewSession(interviewSession);

        //InterviewSession 삭제
        interviewSessionRepository.delete(interviewSession);
    }
}
