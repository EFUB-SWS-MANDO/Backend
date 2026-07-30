package com.example.sprout.domain.profile.service;

import com.example.sprout.domain.file.service.S3FileService;
import com.example.sprout.domain.file.service.S3PresignedUrlService;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.profile.dto.request.CreateProfileRequest;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final S3PresignedUrlService s3PresignedUrlService;
    private final S3FileService s3FileService;

    @Transactional
    public ProfileResponse createProfile(Long memberId, CreateProfileRequest request) {

        Member member = getMemberById(memberId);

        if (profileRepository.existsByMember(member)) {
            log.debug("profile이 이미 존재 - memberId: {}", memberId);
            throw new BusinessException(ProfileErrorCode.PROFILE_ALREADY_EXISTS);
        }

        //생성하는 회원이 보낸 이미지인지 검증
        validateProfileImageKey(memberId, request.profileImage());

        Profile newProfile = request.toEntity(member);
        profileRepository.save(newProfile);
        String profileImageUrl = s3PresignedUrlService.createDownloadUrlOrNull(newProfile.getProfileImage());

        log.info("프로필 생성 성공 - memberId: {}, profileId: {}, nickname: {}, profileImage: {}, bio: {}",
                memberId, newProfile.getId(), newProfile.getNickname(), newProfile.getProfileImage(), newProfile.getBio());
        //신규 프로필: 팔로우/팔로워 0, isMine = true, isFollowing = false
        return ProfileResponse.of(newProfile, profileImageUrl, 0, 0, true, false);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long requesterId, Long targetMemberId) {
        Member targetMember = getMemberById(targetMemberId);

        Profile profile = getProfileByMember(targetMember);

        log.info("프로필 조회 성공 - requesterId: {}, memberId: {}", requesterId, targetMemberId);
        return toProfileResponse(requesterId, targetMember, profile);
    }

    @Transactional
    public ProfileResponse updateProfile(Long requesterId, UpdateProfileRequest request) {
        Member member = getMemberById(requesterId);
        Profile profile = getProfileByMember(member);

        //생성하는 회원이 보낸 이미지인지 검증
        validateProfileImageKey(requesterId, request.profileImage());

        String oldImage = profile.getProfileImage();
        profile.updateProfile(request.nickname(), request.profileImage(), request.bio());
        String newImage = profile.getProfileImage();

        //이미지 수정 시, 기존 이미지 S3에서 삭제
        if (oldImage != null && !oldImage.equals(newImage)) {
            s3FileService.deleteFiles(List.of(oldImage));
        }

        log.info("프로필 수정 성공 - requesterId: {}, profileId: {}", requesterId, profile.getId());
        return toProfileResponse(requesterId, member, profile);
    }

    @Transactional
    public void deleteByMember(Member member) {
        profileRepository.findByMember(member)
                        .ifPresent(profile -> {
                            String profileImage = profile.getProfileImage();
                            profileRepository.deleteByMember(member);
                            if (profileImage != null) s3FileService.deleteFiles(List.of(profileImage));
                        });
    }

    //S3 이미지 key 검증 메소드
    private void validateProfileImageKey(Long memberId, String profileImage) {
        if (profileImage == null || profileImage.isBlank()) return;

        String prefix = "profiles/" + memberId + "/";
        if (!profileImage.startsWith(prefix)) {
            log.warn("허용되지 않은 프로필 이미지 key - memberId: {}, key: {}", memberId, profileImage);
            throw new BusinessException(ProfileErrorCode.INVALID_PROFILE_IMAGE_KEY);
        }
    }

    private ProfileResponse toProfileResponse (Long requesterId, Member targetMember, Profile profile) {

        int followerCount = followRepository.countByFollowee(targetMember);
        int followeeCount = followRepository.countByFollower(targetMember);
        boolean isMe = requesterId.equals(targetMember.getId());
        boolean isFollowing = !isMe && followRepository.existsByFollowerIdAndFolloweeId(requesterId, targetMember.getId());

        String profileImageUrl = s3PresignedUrlService.createDownloadUrlOrNull(profile.getProfileImage());

        return ProfileResponse.of(profile, profileImageUrl, followerCount, followeeCount, isMe, isFollowing);
    }

    //헬퍼 메소드
    public Profile getProfileByMember(Member member) {
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
