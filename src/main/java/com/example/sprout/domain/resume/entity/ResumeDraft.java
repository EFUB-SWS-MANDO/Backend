package com.example.sprout.domain.resume.entity;

import com.example.sprout.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeDraft extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "order_index", nullable = false)
    private Long orderIndex;

    @Column(name = "question", length = 500, nullable = false)
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "description", length = 500)
    private String description;

    @Builder
    public ResumeDraft(Resume resume, Long orderIndex, String question, String answer, String description) {
        this.resume = resume;
        this.orderIndex = orderIndex;
        this.question = question;
        this.answer = answer;
        this.description = description;
    }
}
