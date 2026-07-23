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
        boolean isPrivate,
        boolean deleted,
        LocalDateTime createdAt,
        boolean edited
) {
    public static CommentResponse of(Comment comment, Profile authorProfile, boolean isVisible) {
        String content = comment.isDeleted() ? "삭제된 댓글입니다" : comment.getContent();

        SimpleMemberDto author = (authorProfile != null && isVisible && !comment.isDeleted())
                ? SimpleMemberDto.from(authorProfile)
                : SimpleMemberDto.unknown();

        // 볼 수 없음 & 삭제 x
        if (!isVisible && !comment.isDeleted()) {
            return new CommentResponse(
                    comment.getId(),
                    author,
                    comment.getParent() != null ? comment.getParent().getId() : null,
                    "비밀댓글입니다.",
                    comment.isPrivate(),
                    false,
                    comment.getCreatedAt(),
                    resolveEdited(comment)
            );
        }
        return new CommentResponse(
                comment.getId(),
                author,
                comment.getParent() != null ? comment.getParent().getId() : null,
                content,
                comment.isPrivate(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                resolveEdited(comment)
        );
    }

    private static boolean resolveEdited(Comment comment) {
        if (comment.getUpdatedAt() == null || comment.getCreatedAt() == null) {
            return false;
        }
        return comment.getUpdatedAt().isAfter(comment.getCreatedAt());
    }
}
