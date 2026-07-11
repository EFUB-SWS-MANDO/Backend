package com.example.sprout.domain.profile.service;

import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.service.MemberService;
import com.example.sprout.domain.profile.dto.response.ProfileResponse;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final MemberService memberService;
    private final FollowRepository followRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long requesterId, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Member requester = memberService.findMemberById(requesterId);

        Profile profile = findProfileByMember(member);
        int followerCount = followRepository.countByFollowee(member);
        int followeeCount = followRepository.countByFollower(member);
        boolean isMe = member.equals(requester);

        return ProfileResponse.of(profile, followerCount, followeeCount, isMe);
    }

    //헬퍼 메소드
    public Profile findProfileByMember(Member member) {
        return profileRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND));
    }

}
