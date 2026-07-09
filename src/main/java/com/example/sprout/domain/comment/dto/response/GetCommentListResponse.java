package com.example.sprout.domain.comment.dto.response;

import java.util.List;

public record GetCommentListResponse(
    List<CommentResponse> comments,
    Long nextIdAfter,
    boolean hasNext,
    Long totalElements
) {
    public static GetCommentListResponse of(List<CommentResponse> comments,
                                            Long nextIdAfter, boolean hasNext, Long totalElements) {
        return new GetCommentListResponse(
                comments,
                nextIdAfter,
                hasNext,
                totalElements
        );
    }
}
