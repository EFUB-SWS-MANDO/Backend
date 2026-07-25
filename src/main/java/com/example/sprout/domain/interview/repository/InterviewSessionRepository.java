package com.example.sprout.domain.interview.repository;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    int countAllByMember(Member member);

    List<InterviewSession> findAllByMember(Member member);

    List<InterviewSession> findTop4ByMemberOrderByUpdatedAtDesc(Member member);
}
