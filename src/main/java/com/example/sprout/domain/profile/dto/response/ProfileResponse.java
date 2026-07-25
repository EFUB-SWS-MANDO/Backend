package com.example.sprout.domain.profile.dto.response;

import com.example.sprout.domain.profile.entity.Profile;
import lombok.Builder;

@Builder
public record ProfileResponse (
        Long memberId,
        String nickname,
        String profileImage,
        String bio,
        int followerCount,
        int followeeCount,
        int sproutLevel,
        boolean isMe,
        boolean isFollowing
) {
    public static ProfileResponse of (Profile profile, String profileImage,
                                      int followerCount, int followeeCount, boolean isMe, boolean isFollowing) {
        return ProfileResponse.builder()
                .memberId(profile.getMember().getId())
                .nickname(profile.getNickname())
                .profileImage(profileImage)
                .bio(profile.getBio())
                .followerCount(followerCount)
                .followeeCount(followeeCount)
                .sproutLevel(profile.getSproutLevel())
                .isMe(isMe)
                .isFollowing(isFollowing)
                .build();
    }
}
