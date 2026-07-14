package com.example.sprout.domain.auth.dto.response;

public record TestTokenResponse(
        Long memberId,
        String testAccessToken
) {}
