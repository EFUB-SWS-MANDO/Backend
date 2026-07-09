package com.example.sprout.domain.auth.dto.request;

import com.example.sprout.domain.member.enums.OauthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignInRequest(
        @NotNull (message = "provider는 필수입니다.")
        OauthProvider provider,
        @NotBlank(message = "oauthAccessToken은 필수입니다.")
        String oauthAccessToken) {}
