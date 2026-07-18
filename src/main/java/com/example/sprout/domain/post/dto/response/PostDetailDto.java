package com.example.sprout.domain.post.dto.response;

import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.profile.entity.Profile;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PostDetailDto(
        AuthorDto author,
        Long postId,
        String title,
        String content,
        int likeCount,
        boolean isMine,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetailDto of(Post post, Profile authorProfile, boolean isMine, boolean isFollowing) {
        return PostDetailDto.builder()
                .author(AuthorDto.of(authorProfile, isFollowing))
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(post.getLikeCount())
                .isMine(isMine)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
