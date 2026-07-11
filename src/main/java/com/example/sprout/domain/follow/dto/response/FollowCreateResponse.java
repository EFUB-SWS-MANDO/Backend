package com.example.sprout.domain.follow.dto.response;

import com.example.sprout.domain.follow.entity.Follow;
import lombok.Builder;

@Builder
public record FollowCreateResponse(
        Long followerId,
        Long followeeId
) {
    public static FollowCreateResponse from(Follow follow) {
        return FollowCreateResponse.builder()
                .followerId(follow.getFollower().getId())
                .followeeId(follow.getFollowee().getId())
                .build();
    }
}
