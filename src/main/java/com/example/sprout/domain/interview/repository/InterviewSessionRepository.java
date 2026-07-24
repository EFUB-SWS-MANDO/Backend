package com.example.sprout.domain.interview.repository;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM InterviewSession i WHERE i.member = :member")
    int deleteAllByMember(@Param("member") Member member);
}
