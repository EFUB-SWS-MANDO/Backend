package com.example.sprout.domain.comment.dto.response;

import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.profile.entity.Profile;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        // SimpleMemberDto는 memberId, nickname, profileImage, isFollowing으로 구성
        SimpleMemberDto author,
        Long parentId,
        String content,
        boolean deleted,
        LocalDateTime createdAt,
        boolean edited
) {
    public static CommentResponse of(Comment comment, Profile authorProfile) {
        String content = comment.isDeleted() ? "삭제된 댓글입니다" : comment.getContent();

        SimpleMemberDto author = (authorProfile != null)
                ? SimpleMemberDto.from(authorProfile)
                : SimpleMemberDto.unknown();

        return new CommentResponse(
                comment.getId(),
                author,
                comment.getParent() != null ? comment.getParent().getId() : null,
                content,
                comment.isDeleted(),
                comment.getCreatedAt(),
                resolveEdited(comment)
        );
    }

    private static boolean resolveEdited(Comment comment) {
        return comment.getUpdatedAt().isAfter(comment.getCreatedAt());
    }
}
