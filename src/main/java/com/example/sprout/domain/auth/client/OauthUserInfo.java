package com.example.sprout.domain.auth.client;

import com.example.sprout.domain.member.enums.OauthProvider;

public interface OauthUserInfo {
    String getProviderId(); //각 소셜의 고유 ID(문자열로 통일)
    String getNickname();
    String getProfileImageUrl();
    OauthProvider getProvider();
}
