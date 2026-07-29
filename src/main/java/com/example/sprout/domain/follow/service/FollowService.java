package com.example.sprout.domain.follow.service;

import com.example.sprout.domain.follow.dto.response.FollowCreateResponse;
import com.example.sprout.domain.follow.dto.response.FollowMemberListResponse;
import com.example.sprout.domain.follow.dto.response.FollowMemberResponse;
import com.example.sprout.domain.follow.entity.Follow;
import com.example.sprout.domain.follow.exception.FollowErrorCode;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.common.util.CursorPageUtils;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;

    // 팔로우 생성
    @Transactional
    public FollowCreateResponse createFollow(Long requesterId, Long followeeId) {

        validateNotSelfFollow(requesterId, followeeId);
        validateMembersExist(requesterId);

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

    // 팔로워 목록 조회
    public FollowMemberListResponse getFollowers(
            Long requesterId, Long targetId, Long idAfter, int limit
    ) {
        validateMembersExist(targetId);

        List<Follow> follows = followRepository.findFollowersByFolloweeId(targetId, idAfter, Pageable.ofSize(limit + 1));

        // 팔로워가 없을 경우 (빈 리스트일 경우) 바로 반환
        if(follows.isEmpty()) {
            return FollowMemberListResponse.of(List.of(), null, false);
        }

        boolean hasNext = CursorPageUtils.hasNextPage(follows, limit);
        follows = CursorPageUtils.trimToPageSize(follows, limit, hasNext);
        Long nextIdAfter = CursorPageUtils.resolveNextIdAfter(follows, hasNext, Follow::getId);

        List<Long> followerIds = follows.stream().map(f -> f.getFollower().getId()).toList();

        Map<Long, Profile> profilesMap = fetchProfileMap(followerIds);

        Set<Long> followingIds = followRepository.findFollowingIdsAmong(requesterId, followerIds);

        List<FollowMemberResponse> followMemberResponses = toFollowMemberResponses(
                follows, profilesMap, followingIds, f -> f.getFollower().getId()
        );

        log.info("Followers 목록 조회 완료 - requesterId={}, targetId={}", requesterId, targetId);
        return FollowMemberListResponse.of(followMemberResponses, nextIdAfter, hasNext);
    }

    // 팔로잉 목록 조회
    public FollowMemberListResponse getFollowings(
            Long requesterId, Long targetId, Long idAfter, int limit
    ) {
        validateMembersExist(targetId);

        List<Follow> follows = followRepository.findFollowingsByFollowerId(targetId, idAfter, Pageable.ofSize(limit + 1));

        // 팔로잉이 없을 경우 (빈 리스트일 경우) 바로 반환
        if(follows.isEmpty()) {
            return FollowMemberListResponse.of(List.of(), null, false);
        }

        boolean hasNext = CursorPageUtils.hasNextPage(follows, limit);
        follows = CursorPageUtils.trimToPageSize(follows, limit, hasNext);
        Long nextIdAfter = CursorPageUtils.resolveNextIdAfter(follows, hasNext, Follow::getId);

        List<Long> followeeIds = follows.stream().map(f -> f.getFollowee().getId()).toList();

        Map<Long, Profile> profileMap = fetchProfileMap(followeeIds);

        // 내 팔로잉 목록 조회 시에는 별도 관계 조회 없이 전체를 팔로잉 상태로 처리
        Set<Long> followingIds = (requesterId.equals(targetId))
                ? new HashSet<>(followeeIds)
                : followRepository.findFollowingIdsAmong(requesterId, followeeIds);

        List<FollowMemberResponse> followMemberResponses = toFollowMemberResponses(
                follows, profileMap, followingIds, f -> f.getFollowee().getId()
        );

        log.info("Followings 목록 조회 완료 - requesterId={}, targetId={}", requesterId, targetId);
        return FollowMemberListResponse.of(followMemberResponses, nextIdAfter, hasNext);
    }

    // MemberId를 key로 Profile 매핑
    private Map<Long, Profile> fetchProfileMap(List<Long> memberIds) {
        return profileRepository.findByMemberIdIn(memberIds).stream()
                .collect(Collectors.toMap(p -> p.getMember().getId(), Function.identity()));
    }

    private List<FollowMemberResponse> toFollowMemberResponses(
            List<Follow> follows, Map<Long, Profile> profilesMap,
            Set<Long> followingIds, Function<Follow, Long> memberIdExtractor
    ) {
        return follows.stream()
                .map(f -> {
                    Long memberId = memberIdExtractor.apply(f);
                    boolean isFollowing = followingIds.contains(memberId);
                    return FollowMemberResponse.of(profilesMap.get(memberId), isFollowing);
                })
                .toList();
    }


    //회원 탈퇴 시 팔로잉/팔로우 삭제
    @Transactional
    public void deleteFollowByMember(Member member) {
        followRepository.deleteByFollowerOrFollowee(member);
    }

    // === Helper method ===
    private void validateNotSelfFollow(Long requesterId, Long followeeId) {
        if (requesterId.equals(followeeId)) {
            throw new BusinessException(FollowErrorCode.CANNOT_FOLLOW_SELF);
        }
    }

    private void validateMembersExist(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
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
