package com.example.sprout.domain.member.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void deleteMember(Long memberId) {

        Member member = findById(memberId);
        memberRepository.delete(member);

        log.info("회원탈퇴 성공 - memberId: {}", memberId);
    }

    private Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.debug("존재하지 않는 회원 - memberId: {}", memberId);
                    return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
    }
}
