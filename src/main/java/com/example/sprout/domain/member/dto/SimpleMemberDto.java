package com.example.sprout.domain.member.dto;

import com.example.sprout.domain.profile.entity.Profile;

public record SimpleMemberDto(
        Long memberId,
        String nickname,
        String profileImage,
        boolean isFollowing
) {
    public static SimpleMemberDto of(Profile author, boolean isFollowing) {
        return new SimpleMemberDto(
                author.getMember().getId(),
                author.getNickname(),
                author.getProfileImage(),
                isFollowing
        );
    }
}
