package com.example.sprout.domain.auth.client;

import com.example.sprout.domain.member.enums.OauthProvider;

public interface OauthApiClient {
    OauthUserInfo getUserInfo(String accessToken);
    OauthProvider supportedProvider();
}
