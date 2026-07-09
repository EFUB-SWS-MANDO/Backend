package com.example.sprout.domain.auth.client.kakao;

import com.example.sprout.domain.auth.client.OauthUserInfo;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse (
    Long id,
    @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) implements OauthUserInfo {
    @Override
    public String getProviderId() {return String.valueOf(id);}

    @Override
    public String getNickname() {return kakaoAccount.profile().nickname();}

    @Override
    public String getProfileImageUrl() {return kakaoAccount.profile().profileImageUrl();}

    @Override
    public OauthProvider getProvider() {return OauthProvider.KAKAO;}

    public record KakaoAccount(KakaoProfile profile) {
        public record KakaoProfile(
                String nickname,
                @JsonProperty("profile_image_url") String profileImageUrl
        ) {}
    }
}
