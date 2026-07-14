package com.example.sprout.domain.profile.service;

import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.profile.dto.request.CreateProfileRequest;
import com.example.sprout.domain.profile.dto.response.CreateProfileResponse;
import com.example.sprout.domain.profile.dto.response.ProfileResponse;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ProfileService profileService;

    Long memberId;
    Member member;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", memberId);
    }

    @Nested
    @DisplayName("프로필 생성")
    class CreateProfile {
        CreateProfileRequest request;

        @BeforeEach
        void setUp() {
            request = new CreateProfileRequest("nickname","profile.png", "bio");
        }

        @Test
        @DisplayName("프로필 생성 성공")
        void  createProfile_Success() {
            //given
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            //아직 프로필 생성X
            given(profileRepository.existsByMember(member)).willReturn(false);
            given(profileRepository.save(any(Profile.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            //when
            CreateProfileResponse response = profileService.createProfile(memberId, request);

            //then
            AssertionsForClassTypes.assertThat(response).isNotNull();

            ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
            verify(profileRepository).save(captor.capture());
            Profile savedProfile = captor.getValue();

            AssertionsForClassTypes.assertThat(savedProfile.getMember()).isEqualTo(member);
            AssertionsForClassTypes.assertThat(savedProfile.getNickname()).isEqualTo(request.nickname());
            AssertionsForClassTypes.assertThat(savedProfile.getProfileImage()).isEqualTo(request.profileImage());
            AssertionsForClassTypes.assertThat(savedProfile.getBio()).isEqualTo(request.bio());

            verify(memberRepository).findById(memberId);
            verify(profileRepository).existsByMember(member);
        }

        @Test
        @DisplayName("이미 프로필이 존재하는 회원이 프로필 생성 시도 시 실패")
        void createProfile_AlreadyExists_Fail() {
            //given
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(profileRepository.existsByMember(member)).willReturn(true);

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> profileService.createProfile(memberId, request)
            );
            AssertionsForClassTypes.assertThat(exception.getErrorCode()).isEqualTo(ProfileErrorCode.PROFILE_ALREADY_EXISTS);
            verify(profileRepository, never()).save(any(Profile.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원이 프로필 생성 시도 시 실패")
        void createProfile_MemberNotFound_Fail() {
            //given
            given(memberRepository.findById(memberId))
                    .willThrow(new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> profileService.createProfile(memberId, request)
            );
            AssertionsForClassTypes.assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
            verify(profileRepository, never()).existsByMember(any());
            verify(profileRepository, never()).save(any(Profile.class));
        }


    }
    @Nested
    @DisplayName("프로필 조회")
    class GetProfile {

        Long requesterId;
        Member requester;
        Profile profile;


        @BeforeEach
        void setUp() {
            requesterId = 2L;
            requester = Member.builder().build();
            ReflectionTestUtils.setField(requester,"id", requesterId);

            profile = Profile.builder()
                    .member(requester)
                    .nickname("nickname")
                    .profileImage("profile.png")
                    .bio("bio")
                    .build();

        }

        @Test
        @DisplayName("프로필 조회 성공 - isMe = false")
        void getProfile_Others_Success() {
            //given
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(profileRepository.findByMember(member)).willReturn(Optional.of(profile));
            given(followRepository.countByFollowee(member)).willReturn(10);
            given(followRepository.countByFollower(member)).willReturn(5);
            //when
            ProfileResponse response = profileService.getProfile(requesterId, memberId);
            //then
            assertThat(response).isNotNull();
            assertThat(response.followerCount()).isEqualTo(10);
            assertThat(response.followeeCount()).isEqualTo(5);
            assertThat(response.isMe()).isFalse();
            verify(memberRepository).findById(memberId);
            verify(memberRepository).findById(requesterId);
        }
        @Test
        @DisplayName("프로필 조회 성공 - isMe = true")
        void getProfile_Me_Success() {
            //given
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(profileRepository.findByMember(member)).willReturn(Optional.of(profile));
            given(followRepository.countByFollower(member)).willReturn(0);
            given(followRepository.countByFollowee(member)).willReturn(0);

            //when
            ProfileResponse response = profileService.getProfile(memberId, memberId);

            //then
            assertThat(response.isMe()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 프로필 조회 시 실패")
        void getProfile_ProfileNotFound_Fail() {
            //given
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(profileRepository.findByMember(member)).willReturn(Optional.empty());
            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> profileService.getProfile(requesterId, memberId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ProfileErrorCode.PROFILE_NOT_FOUND);

            verifyNoInteractions(followRepository);
        }
        @Test
        @DisplayName("존재하지 않는 회원의 프로필 조회 시 실패")
        void getProfile_MemberNotFound_Fail() {
            //given
            given(memberRepository.findById(memberId))
                    .willThrow(new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> profileService.getProfile(requesterId, memberId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository, never()).findById(requesterId);
            verifyNoInteractions(profileRepository, followRepository);

        }
    }

}