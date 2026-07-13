package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.repository.InterviewAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewAnswerService {

    private final InterviewAnswerRepository interviewAnswerRepository;

    @Transactional
    public void deleteAllByInterviewSession(InterviewSession interviewSession) {
        interviewAnswerRepository.deleteAllBySession(interviewSession);
    }
}
