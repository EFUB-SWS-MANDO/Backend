package com.example.sprout.domain.auth.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateMemberResponse (
    Long memberId,
    String accessToken,
    String refreshToken,
    long expiresIn,
    LocalDateTime expiresAt,
    boolean isNewUser
) {}