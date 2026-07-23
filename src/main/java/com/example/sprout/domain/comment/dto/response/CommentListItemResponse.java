package com.example.sprout.domain.comment.dto.response;

import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.profile.entity.Profile;

public record CommentListItemResponse(
        CommentResponse commentResponse,
        boolean hasChildren
) {
    public static CommentListItemResponse of(Comment comment, Profile authorProfile, boolean isVisible, boolean hasChildren) {
        return new CommentListItemResponse(
                CommentResponse.of(comment, authorProfile, isVisible),
                hasChildren
        );
    }
}
