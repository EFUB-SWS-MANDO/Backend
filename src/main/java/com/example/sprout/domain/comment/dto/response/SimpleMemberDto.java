package com.example.sprout.domain.comment.dto.response;

import com.example.sprout.domain.profile.entity.Profile;

public record SimpleMemberDto(
        Long memberId,
        String nickname,
        String profileImage
) {
    public static SimpleMemberDto from(Profile authorProfile) {
        return new SimpleMemberDto(
                authorProfile.getMember().getId(),
                authorProfile.getNickname(),
                authorProfile.getProfileImage()
        );
    }
}
