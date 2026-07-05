package com.example.sprout.domain.interview.entity;

import com.example.sprout.global.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "interview_answers",
        indexes = {
                @Index(name = "idx_interview_session_id", columnList = "session_id"),
                @Index(name = "idx_interview_question_id", columnList = "question_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewAnswer extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, updatable = false)
    private InterviewSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, updatable = false, unique = true)
    private InterviewQuestion question;

    @Column(name = "content", nullable = false)
    private String content;

    @Builder
    public InterviewAnswer(InterviewSession session, InterviewQuestion question, String content) {
        this.session = session;
        this.question = question;
        this.content = content;
    }

}
