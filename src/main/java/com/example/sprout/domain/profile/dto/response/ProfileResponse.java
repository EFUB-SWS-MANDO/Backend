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
        boolean isMe
) {
    public static ProfileResponse of (Profile profile, int followerCount, int followeeCount, boolean isMe) {
        return ProfileResponse.builder()
                .memberId(profile.getMember().getId())
                .nickname(profile.getNickname())
                .profileImage(profile.getProfileImage())
                .bio(profile.getBio())
                .followerCount(followerCount)
                .followeeCount(followeeCount)
                .sproutLevel(profile.getSproutLevel())
                .isMe(isMe)
                .build();
    }
}
