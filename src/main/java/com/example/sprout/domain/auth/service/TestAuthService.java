package com.example.sprout.domain.auth.service;

import com.example.sprout.domain.auth.dto.response.TestTokenResponse;
import com.example.sprout.domain.auth.jwt.JwtUtil;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Service
@RequiredArgsConstructor
@Slf4j
public class TestAuthService {

    private final MemberRepository memberRepository;
    private  final JwtUtil jwtUtil;

    int testMemberCount = 1;

    @Transactional
    public String getTestTokenByMemberId(Long memberId) {
        if(!memberRepository.existsById(memberId)) {
            log.warn("존재하지 않는 회원 - memberId: {}", memberId);
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        return jwtUtil.createAccessToken(memberId);
    }

    @Transactional
    public Long createTestMember() {
        Member testMember = Member.builder()
                .oauthId("testMember"+testMemberCount)
                .oauthProvider(OauthProvider.KAKAO)
                .build();
        memberRepository.save(testMember);
        testMemberCount++;

        return testMember.getId();
    }

    @Transactional
    public TestTokenResponse getTestTokenAndMember() {
        Long testMemberId = createTestMember();
        String testAccessToken = getTestTokenByMemberId(testMemberId);

        return new TestTokenResponse(testMemberId, testAccessToken);
    }
}
