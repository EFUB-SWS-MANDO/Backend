package com.example.sprout.domain.post.dto.response;

public record PostLikeResponse(
        Long postId,
        int likeCount
) {}
