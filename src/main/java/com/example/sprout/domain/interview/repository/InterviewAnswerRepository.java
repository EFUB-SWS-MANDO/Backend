package com.example.sprout.domain.interview.repository;

import com.example.sprout.domain.interview.entity.InterviewAnswer;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM InterviewAnswer i WHERE i.session = :session")
    int deleteAllBySession(@Param("session") InterviewSession interviewSession);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM InterviewAnswer i WHERE i.session.member = :member")
    int deleteAllByMember(@Param("member") Member member);
}
