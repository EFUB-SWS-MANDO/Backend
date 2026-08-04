package com.example.sprout.domain.follow.dto.response;

import com.example.sprout.domain.profile.entity.Profile;

public record FollowMemberResponse(
        Long memberId,
        String nickname,
        String profileImage,
        boolean isFollowing
) {
    public static FollowMemberResponse of(Profile profile, boolean isFollowing) {
        return new FollowMemberResponse(
                profile.getMember().getId(),
                profile.getNickname(),
                profile.getProfileImage(),
                isFollowing
        );
    }
}
