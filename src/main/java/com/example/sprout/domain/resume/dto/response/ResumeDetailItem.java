package com.example.sprout.domain.resume.dto.response;

import com.example.sprout.domain.resume.entity.ResumeDraft;

public record ResumeDetailItem (
        Long questionId,
        Long order,
        String content,
        String answer,
        String description
){
    public static ResumeDetailItem from(ResumeDraft resumeDraft) {
        return new ResumeDetailItem(
                resumeDraft.getId(),
                resumeDraft.getOrderIndex(),
                resumeDraft.getQuestion(),
                resumeDraft.getAnswer(),
                resumeDraft.getDescription()
        );
    }
}