package com.example.sprout.domain.profile.service;

import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.profile.dto.request.CreateProfileRequest;
import com.example.sprout.domain.profile.dto.response.CreateProfileResponse;
import com.example.sprout.domain.member.service.MemberService;
import com.example.sprout.domain.profile.dto.request.UpdateProfileRequest;
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
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Transactional
    public CreateProfileResponse createProfile(Long memberId, CreateProfileRequest request) {

        Member member = getMemberById(memberId);

        if (profileRepository.existsByMember(member)) {
            log.debug("profile이 이미 존재 - memberId: {}", memberId);
            throw new BusinessException(ProfileErrorCode.PROFILE_ALREADY_EXISTS);
        }

        Profile newProfile = request.toEntity(member);
        profileRepository.save(newProfile);

        log.info("프로필 생성 성공 - memberId: {}, profileId: {}, nickname: {}, profileImage: {}, bio: {}",
                memberId, newProfile.getId(), newProfile.getNickname(), newProfile.getProfileImage(), newProfile.getBio());
        return new CreateProfileResponse(newProfile.getId());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long requesterId, Long targetMemberId) {
        Member targetMember = getMemberById(targetMemberId);

        Profile profile = findProfileByMember(targetMember);
        int followerCount = followRepository.countByFollowee(targetMember);
        int followeeCount = followRepository.countByFollower(targetMember);
        boolean isMe = targetMember.getId().equals(requesterId);

        log.info("프로필 조회 성공 - requesterId: {}, memberId: {}", requesterId, targetMemberId);
        return ProfileResponse.of(profile, followerCount, followeeCount, isMe);
    }

    @Transactional
    public ProfileResponse updateProfile(Long requesterId, UpdateProfileRequest request) {
        Member member = getMemberById(requesterId);
        Profile profile = findProfileByMember(member);

        profile.updateProfile(request.nickname(), request.profileImage(), request.bio());

        int followerCount = followRepository.countByFollowee(member);
        int followeeCount = followRepository.countByFollower(member);

        log.info("프로필 수정 성공 - requesterId: {}, profileId: {}", requesterId, profile.getId());
        return ProfileResponse.of(profile, followerCount, followeeCount, true);
    }

    @Transactional
    public void deleteByMember(Member member) {
        profileRepository.deleteByMember(member);
    }

    //헬퍼 메소드
    public Profile findProfileByMember(Member member) {
        return profileRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.debug("존재하지 않는 프로필 조회 시도 - memberId: {}", member.getId());
                    return new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND);
                });
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.debug("존재하지 않는 회원 - memberId: {}", memberId);
                    return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
    }
}
