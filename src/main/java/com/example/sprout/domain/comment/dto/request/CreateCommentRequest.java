package com.example.sprout.domain.comment.dto.request;

import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 300, message = "댓글은 300자를 초과할 수 없습니다.")
        String content,
        Long parentId
) {
    public Comment toEntity(Member author, Post post, Comment parent) {
        return Comment.builder()
                .author(author)
                .post(post)
                .parent(parent)
                .content(content)
                .build();
    }
}
