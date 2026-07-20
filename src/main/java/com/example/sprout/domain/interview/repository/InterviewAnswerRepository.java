package com.example.sprout.domain.interview.repository;

import com.example.sprout.domain.interview.entity.InterviewAnswer;
import com.example.sprout.domain.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {
    void deleteAllBySession(InterviewSession interviewSession);
}
