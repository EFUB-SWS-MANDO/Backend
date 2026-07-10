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

    // 탈퇴한 사용자에 대한 정보
    public static SimpleMemberDto withdrawn() {
        return new SimpleMemberDto(null, "알 수 없음", null);
    }
}
