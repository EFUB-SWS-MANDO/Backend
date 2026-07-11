package com.example.sprout.domain.comment.dto.response;

import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.dto.response.SimpleMemberDto;
import com.example.sprout.domain.profile.entity.Profile;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        // SimpleMemberDto는 memberId, nickname, profileImage, isFollowing으로 구성
        SimpleMemberDto author,
        Long parentId,
        String content,
        LocalDateTime updatedAt
) {
    public static CommentResponse of(Comment comment, Profile authorProfile) {
        return new CommentResponse(
                comment.getId(),
                SimpleMemberDto.from(authorProfile),
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getContent(),
                comment.getUpdatedAt()
        );
    }
}
