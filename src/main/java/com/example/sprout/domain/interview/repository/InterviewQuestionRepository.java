package com.example.sprout.domain.interview.repository;

import com.example.sprout.domain.interview.dto.response.InterviewQnaHistoryResponse;
import com.example.sprout.domain.interview.entity.InterviewQuestion;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM InterviewQuestion i WHERE i.session = :session")
    int deleteAllBySession(@Param("session") InterviewSession interviewSession);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM InterviewQuestion i WHERE i.session.member = :member")
    int deleteAllByMember(@Param("member") Member member);

    @Query("""
        SELECT new com.example.sprout.domain.interview.dto.response.InterviewQnaHistoryResponse(
                q.content,
                a.content
                )
        FROM InterviewQuestion q
        LEFT JOIN InterviewAnswer a
        ON a.question = q
        WHERE q.session.id = :sessionId
        ORDER BY q.id ASC
        """)
    List<InterviewQnaHistoryResponse> findQnaHistoryBySessionId(@Param("sessionId") Long sessionId);

    List<InterviewQuestion> findAllBySessionOrderByIdAsc(InterviewSession interviewSession);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM InterviewQuestion q WHERE q.id = :questionId")
    Optional<InterviewQuestion> findByIdWithLock(@Param("questionId") Long interviewQuestionId);
}
