package com.example.sprout.domain.resume.dto.response;

import com.example.sprout.domain.resume.entity.Resume;

import java.util.List;

public record GetResumeListResponse(
        List<ResumeListItem> resumes,
        Long nextIdAfter,
        boolean hasNext,
        Long totalElements
) {
    public static GetResumeListResponse of(List<ResumeListItem> resumeList, Long nextIdAfter, boolean hasNext, Long totalElements) {
        return new GetResumeListResponse(
                resumeList,
                nextIdAfter,
                hasNext,
                totalElements
        );
    }
}
