package com.example.sprout.domain.post.dto.request;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
    @NotBlank(message = "게시글 제목은 필수입니다.")
    @Size(min=1, max=20, message = "게시글의 제목은 1자 이상, 20자 이하입니다.")
    String title,
    @NotBlank(message = "게시글 내용이 없습니다.")
    String content,
    List<String> categories,
    List<String> fileKeys,
    @NotNull(message = "공개 및 비공개 여부 선택은 필수입니다.")
    boolean isPrivate

) {
    public Post toEntity(Member author) {
        return Post.builder()
                .author(author)
                .title(title)
                .content(content)
                .isPrivate(isPrivate)
                .build();
    }
}
