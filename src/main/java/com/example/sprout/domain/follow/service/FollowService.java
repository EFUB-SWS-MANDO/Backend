package com.example.sprout.domain.follow.service;

import com.example.sprout.domain.follow.dto.response.FollowCreateResponse;
import com.example.sprout.domain.follow.entity.Follow;
import com.example.sprout.domain.follow.exception.FollowErrorCode;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
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
        validateDuplicateFollow(requesterId, followeeId);

        Member requester = getMember(requesterId);
        Member followee = getMember(followeeId);

        Follow follow = Follow.builder().follower(requester).followee(followee).build();
        Follow savedFollow = saveFollow(follow);

        log.info("Follow 생성 완료 - followerId={}, followeeId={}", requesterId, followeeId);

        return FollowCreateResponse.from(savedFollow);
    }

    // 팔로우 취소
    @Transactional
    public void deleteFollow(Long requesterId, Long followeeId) {

        Follow follow = getFollow(requesterId, followeeId);

        followRepository.delete(follow);

        log.info("Follow 취소 완료 - followerId={}, followeeId={}", requesterId, followeeId);

    }

    // === Helper method ===
    private Member getMember(Long memberId) {
        // TODO: Member 도메인 Not Found ErrorCode로 수정
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(GlobalErrorCode.RESOURCE_NOT_FOUND));
    }

    private Follow getFollow(Long followerId, Long followeeId) {
        return followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .orElseThrow(() -> new BusinessException(FollowErrorCode.FOLLOW_NOT_FOUND));
    }

    private void validateNotSelfFollow(Long requesterId, Long followeeId) {
        if (requesterId.equals(followeeId)) {
            throw new BusinessException(FollowErrorCode.CANNOT_FOLLOW_SELF);
        }
    }

    private void validateDuplicateFollow(Long requesterId, Long followeeId) {
        if (followRepository.existsByFollowerIdAndFolloweeId(requesterId, followeeId)) {
            throw new BusinessException(FollowErrorCode.FOLLOW_ALREADY_EXISTS);
        }
    }

    private Follow saveFollow(Follow follow) {
        try {
            return followRepository.save(follow);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(FollowErrorCode.FOLLOW_ALREADY_EXISTS);
        }
    }

}
