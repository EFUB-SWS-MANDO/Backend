package com.example.sprout.domain.auth.client.kakao;

import com.example.sprout.domain.auth.AuthErrorCode;
import com.example.sprout.domain.auth.client.OauthApiClient;
import com.example.sprout.domain.auth.client.OauthUserInfo;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OauthApiClient {

    private final WebClient webClient;

    @Override
    public OauthUserInfo getUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserInfoResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new BusinessException(AuthErrorCode.INVALID_KAKAO_TOKEN);
        }
    }

    @Override
    public OauthProvider supportedProvider() {
        return OauthProvider.KAKAO;
    }
}
