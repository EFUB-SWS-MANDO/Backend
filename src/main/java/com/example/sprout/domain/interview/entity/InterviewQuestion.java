package com.example.sprout.domain.interview.entity;

import com.example.sprout.domain.interview.enums.InterviewQuestionType;
import com.example.sprout.global.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "interview_questions",
        indexes = {
                @Index(name = "idx_interview_session_id", columnList = "session_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, updatable = false)
    private InterviewSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private InterviewQuestionType type;

    @Column(name = "content")
    private String content;

    @Builder
    public InterviewQuestion(InterviewSession session, InterviewQuestionType type, String content) {
        this.session = session;
        this.type = type;
        this.content = content;
    }

    public void recordContent(String content) {
        this.content = content;
    }

}
