package com.example.sprout.domain.interview.entity;

import com.example.sprout.domain.interview.enums.InterviewSessionStatus;
import com.example.sprout.domain.interview.enums.InterviewSessionType;
import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.global.common.entity.BaseTimeEntity;
import com.example.sprout.global.error.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "interview_sessions",
        indexes = {
                @Index(name = "idx_interview_session_member_id", columnList = "member_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewSessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private InterviewSessionType type;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "feedback_summary")
    private String feedbackSummary;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Builder
    public InterviewSession(Member member, InterviewSessionType type){
        this.status = InterviewSessionStatus.IN_PROGRESS; // 생성 시 기본 설정 - 진행 중

        this.member = member;
        this.type = type;
    }

    public void complete() {
        this.status = InterviewSessionStatus.COMPLETED;
    }

    public void recordSummary(String summary){
        validateCompleted();
        this.summary = summary;
    }

    public void recordFeedbackResult(String feedbackSummary, String feedback){
        validateCompleted();

        this.feedbackSummary = feedbackSummary;
        this.feedback = feedback;
    }

    private void validateCompleted() {
        if (status != InterviewSessionStatus.COMPLETED) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_NOT_COMPLETED);
        }
    }
}
