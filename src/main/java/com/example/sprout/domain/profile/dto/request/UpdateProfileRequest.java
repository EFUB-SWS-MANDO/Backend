package com.example.sprout.domain.profile.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateProfileRequest(
        @Size(min=2, max=10, message = "닉네임은 2자 이상 10자 이하여야합니다.")
        String nickname,
        String profileImage,
        @Size(max=150, message = "자기소개는 150자 이하입니다.")
        String bio
) {}