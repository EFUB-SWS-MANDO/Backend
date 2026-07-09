package com.example.sprout.domain.auth.service;

import com.example.sprout.domain.auth.AuthErrorCode;
import com.example.sprout.domain.auth.client.OauthApiClient;
import com.example.sprout.domain.auth.client.OauthUserInfo;
import com.example.sprout.domain.auth.dto.request.SignInRequest;
import com.example.sprout.domain.auth.dto.request.ReissueTokenRequest;
import com.example.sprout.domain.auth.dto.response.SignInResponse;
import com.example.sprout.domain.auth.dto.response.ReissueTokenResponse;
import com.example.sprout.domain.auth.jwt.JwtUtil;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.global.error.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    public SignInResponse signIn(SignInRequest request) {
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

        //accessToken, refreshToken 생성
        String accessToken = jwtUtil.createAccessToken(member.getId());
        String refreshToken = jwtUtil.createRefreshToken(member.getId());

        //refreshToken Redis 저장
        saveRefreshToken(member.getId(), refreshToken);

        long expiresIn = jwtUtil.getAccessExpirationMs()/1000;
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

        log.info("로그인 성공 - memberId: {}, provider: {}, isNewUser: {}",
                member.getId(), request.provider(), isNewUser);

        return SignInResponse.builder()
                .memberId(member.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .isNewUser(isNewUser)
                .build();
    }

    public void signOut(Long memberId, HttpServletRequest request) {
        String accessToken = JwtUtil.resolveToken(request);
        //refreshToken 삭제
        redisTemplate.delete("refresh:"+memberId);

        //accessToken 블랙리스트 등록
        Claims claims = jwtUtil.validateToken(accessToken);
        if (claims != null) {
            long remainingMs = jwtUtil.getRemainingExpiration(claims);
            redisTemplate.opsForValue().set(
                    "blacklist:"+accessToken,
                    "logout",
                    Duration.ofMillis(remainingMs)
            );
        }

        log.info("로그아웃 성공 - memberId: {}", memberId);
    }

    public ReissueTokenResponse reissueToken(ReissueTokenRequest request) {
        String refreshToken = request.refreshToken();
        Claims claims = jwtUtil.validateToken(refreshToken);
        if (claims == null) {
            log.warn("유효하지 않은 refreshToken으로 재발급 시도");
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        Long memberId = claims.get("memberId", Long.class);
        String savedToken = redisTemplate.opsForValue().get("refresh:"+memberId);

        if (savedToken == null) {
            log.warn("Redis에 저장된 토큰 없음 - memberId: {}", memberId);
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
        else if (!savedToken.equals(refreshToken)) {
            log.warn("Redis의 refreshToken과 요청 refreshToken 불일치 - memberId: {}", memberId);
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtUtil.createAccessToken(memberId);
        long expiresIn = jwtUtil.getAccessExpirationMs()/1000;
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

        log.info("토큰 재발급 성공 - memberId: {}", memberId);


        return ReissueTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .build();
    }

    private OauthApiClient findClient(OauthProvider provider) {
        return oauthApiClientList.stream()
                .filter(client -> client.supportedProvider() == provider)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("지원하지 않는 oauthProvider 요청 - provider: {}", provider);
                    return new BusinessException(AuthErrorCode.UNSUPPORTED_PROVIDER);
                });
    }

    //Redis에 RefreshToken 저장
    private void saveRefreshToken(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set(
                "refresh:" + memberId,
                refreshToken,
                Duration.ofMillis(jwtUtil.getRefreshExpirationMs())
        );
    }
}
