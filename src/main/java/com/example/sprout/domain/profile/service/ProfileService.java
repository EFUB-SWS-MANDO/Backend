package com.example.sprout.domain.profile.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.service.MemberService;
import com.example.sprout.domain.profile.dto.request.CreateProfileRequest;
import com.example.sprout.domain.profile.dto.response.CreateProfileResponse;
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

    @Transactional
    public CreateProfileResponse createProfile(Long memberId, CreateProfileRequest request) {

        Member member = memberService.getMemberById(memberId);

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

    @Transactional
    public void deleteByMember(Member member) {
        profileRepository.deleteByMember(member);
    }
}
