package com.example.sprout.domain.comment.dto.response;

public record SimpleMemberDto(
        Long memberId,
        String nickname,
        String profileImage
) {
    public static SimpleMemberDto of(Long memberId, String nickname, String profileImage) {
        return new SimpleMemberDto(memberId, nickname, profileImage);
    }

    // 탈퇴한 사용자에 대한 정보
    public static SimpleMemberDto unknown() {
        return new SimpleMemberDto(null, "알 수 없음", null);
    }
}
