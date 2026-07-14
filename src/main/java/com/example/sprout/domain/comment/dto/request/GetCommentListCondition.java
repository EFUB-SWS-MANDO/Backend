package com.example.sprout.domain.comment.dto.request;

public record GetCommentListCondition(
        Long idAfter,
        Integer limit
) {
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 10;

    public GetCommentListCondition {

        if (limit == null) {
            limit = DEFAULT_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
    }
}
