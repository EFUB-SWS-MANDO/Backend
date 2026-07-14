package com.example.sprout.domain.follow.service;

import com.example.sprout.domain.follow.dto.response.FollowCreateResponse;
import com.example.sprout.domain.follow.entity.Follow;
import com.example.sprout.domain.follow.exception.FollowErrorCode;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.global.error.BusinessException;
import com.example.sprout.global.error.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    // 팔로우 생성
    @Transactional
    public FollowCreateResponse createFollow(Long requesterId, Long followeeId) {

        validateNotSelfFollow(requesterId, followeeId);
        validateMembersExist(requesterId, followeeId);

        Member requester = memberRepository.getReferenceById(requesterId);
        Member followee = memberRepository.getReferenceById(followeeId);

        Follow follow = Follow.builder().follower(requester).followee(followee).build();
        Follow savedFollow = saveFollow(follow);

        log.info("Follow 생성 완료 - followerId={}, followeeId={}", requesterId, followeeId);

        return FollowCreateResponse.from(savedFollow);
    }

    // 팔로우 취소
    @Transactional
    public void deleteFollow(Long requesterId, Long followeeId) {

        removeFollow(requesterId, followeeId);

        log.info("Follow 취소 완료 - followerId={}, followeeId={}", requesterId, followeeId);

    }

    //회원 탈퇴 시 팔로잉/팔로우 삭제
    @Transactional
    public void deleteFollowByMember(Member member) {
        followRepository.deleteByFollowerOrFollowee(member);
    }

    // === Helper method ===
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateNotSelfFollow(Long requesterId, Long followeeId) {
        if (requesterId.equals(followeeId)) {
            throw new BusinessException(FollowErrorCode.CANNOT_FOLLOW_SELF);
        }
    }

    private void validateMembersExist(Long requesterId, Long followeeId) {
        if (!memberRepository.existsById(requesterId) || !memberRepository.existsById(followeeId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private Follow saveFollow(Follow follow) {
        try {
            return followRepository.save(follow);
        } catch (DataIntegrityViolationException e) {
            // 이미 팔로우하는 관계가 있을 경우 에러
            throw new BusinessException(FollowErrorCode.FOLLOW_ALREADY_EXISTS);
        }
    }

    private void removeFollow(Long requesterId, Long followeeId) {
        int deletedCount = followRepository.deleteByFollowerIdAndFolloweeId(requesterId, followeeId);

        if (deletedCount == 0) {
            throw new BusinessException(FollowErrorCode.FOLLOW_NOT_FOUND);
        }
    }

}
