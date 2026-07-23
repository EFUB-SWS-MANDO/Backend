package com.example.sprout.domain.post.dto.response;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.profile.entity.Profile;
import lombok.Builder;

@Builder
public record AuthorDto(
        Long memberId,
        String nickname,
        String profileImage,
        boolean isFollowing
) {
    public static AuthorDto of (Profile authorProfile, boolean isFollowing) {
        return AuthorDto.builder()
                .memberId(authorProfile.getMember().getId())
                .nickname(authorProfile.getNickname())
                .profileImage(authorProfile.getProfileImage())
                .isFollowing(isFollowing)
                .build();
    }
}
