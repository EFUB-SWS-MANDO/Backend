package com.example.sprout.domain.comment.dto.response;

import com.example.sprout.domain.comment.entity.Comment;

public record CommentListItemResponse(
        CommentResponse commentResponse,
        boolean hasChildren
) {
    public static CommentListItemResponse of(Comment comment, SimpleMemberDto author, boolean isVisible, boolean hasChildren) {
        return new CommentListItemResponse(
                CommentResponse.of(comment, author, isVisible),
                hasChildren
        );
    }
}
