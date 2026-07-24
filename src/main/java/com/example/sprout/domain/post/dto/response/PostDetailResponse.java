package com.example.sprout.domain.post.dto.response;

import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.profile.entity.Profile;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostDetailResponse(
        AuthorDto author,
        Long postId,
        String title,
        String content,
        List<String> fileKeys,
        List<String> categories,
        int likeCount,
        boolean isMine,
        boolean isLike,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isUpdated,
        boolean isPrivate
) {
    public static PostDetailResponse of(Post post, Profile authorProfile, List<String> fileKeys , List<String> categories,
                                        boolean isMine, boolean isFollowing, boolean isLike) {
        return PostDetailResponse.builder()
                .author(AuthorDto.of(authorProfile, isFollowing))
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .fileKeys(fileKeys)
                .categories(categories)
                .likeCount(post.getLikeCount())
                .isMine(isMine)
                .isLike(isLike)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isUpdated(post.isUpdated())
                .isPrivate(post.isPrivate())
                .build();
    }
}
