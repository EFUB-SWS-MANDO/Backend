package com.example.sprout.domain.auth.service;

import com.example.sprout.domain.auth.client.OauthApiClient;
import com.example.sprout.domain.auth.client.OauthUserInfo;
import com.example.sprout.domain.auth.dto.request.CreateMemberRequest;
import com.example.sprout.domain.auth.dto.response.CreateMemberResponse;
import com.example.sprout.domain.auth.jwt.JwtUtil;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.global.error.BusinessException;
import com.example.sprout.global.error.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {
    private final List<OauthApiClient> oauthApiClientList;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    public CreateMemberResponse signIn(CreateMemberRequest request) {
        OauthApiClient client = findClient(request.provider());
        OauthUserInfo userInfo = client.getUserInfo(request.oauthAccessToken());

        Optional<Member> existingMember = memberRepository.findByOauthIdAndOauthProvider(userInfo.getProviderId(), request.provider());
        boolean isNewUser = existingMember.isEmpty();

        Member member = existingMember.orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .oauthProvider(request.provider())
                                .oauthId(userInfo.getProviderId())
                                .build()
                ));

        String accessToken = jwtUtil.createAccessToken(member.getId());
        String refreshToken = jwtUtil.createRefreshToken(member.getId());

        long expiresIn = jwtUtil.getAccessExpirationMs()/1000;
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

        log.info("로그인 성공 - memberId: {}, provider: {}, isNewUser: {}",
                member.getId(), request.provider(), isNewUser);

        return CreateMemberResponse.builder()
                .memberId(member.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .isNewUser(isNewUser)
                .build();
    }

    private OauthApiClient findClient(OauthProvider provider) {
        return oauthApiClientList.stream()
                .filter(client -> client.supportedProvider() == provider)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("지원하지 않는 oauthProvider 요청 - provider: {}", provider);
                    return new BusinessException(GlobalErrorCode.UNSUPPORTED_PROVIDER);
                });
    }
}
