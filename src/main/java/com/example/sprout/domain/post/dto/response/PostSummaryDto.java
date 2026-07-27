package com.example.sprout.domain.post.dto.response;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.profile.entity.Profile;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostSummaryDto(
        AuthorDto author,
        Long postId,
        String title,
        String summary,
        List<String> categories,
        boolean isLike,
        int likeCount,
        Long commentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isUpdated,
        boolean isPrivate

) {
    private static final int SUMMARY_LENGTH = 100;

    public static PostSummaryDto of (Post post, AuthorDto author, List<String> categories, Long commentCount,
                                     boolean isLike) {
        return PostSummaryDto.builder()
                .author(author)
                .postId(post.getId())
                .title(post.getTitle())
                .summary(truncate(post.getContent(), SUMMARY_LENGTH))
                .categories(categories)
                .isLike(isLike)
                .likeCount(post.getLikeCount())
                .commentCount(commentCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .isUpdated(post.isUpdated())
                .isPrivate(post.isPrivate())
                .build();
    }

    private static String truncate(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
}
