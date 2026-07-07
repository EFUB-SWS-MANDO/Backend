package com.example.sprout.domain.interview.repository;

import com.example.sprout.domain.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
}
