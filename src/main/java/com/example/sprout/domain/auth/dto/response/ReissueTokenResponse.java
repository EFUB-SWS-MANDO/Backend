package com.example.sprout.domain.auth.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReissueTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        LocalDateTime expiresAt
) {}
