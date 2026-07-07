package com.example.sprout.domain.auth.dto.request;

import com.example.sprout.domain.member.enums.OauthProvider;
import lombok.Getter;

public record CreateMemberRequest(
        OauthProvider provider,
        String kakaoAccessToken) {}
