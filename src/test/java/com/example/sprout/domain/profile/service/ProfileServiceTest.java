package com.example.sprout.domain.profile.service;

import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.service.MemberService;
import com.example.sprout.domain.profile.dto.response.ProfileResponse;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private FollowRepository followRepository;

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
            given(memberService.findMemberById(memberId)).willReturn(member);
            given(memberService.findMemberById(requesterId)).willReturn(requester);
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

            verify(memberService).findMemberById(memberId);
            verify(memberService).findMemberById(requesterId);
        }

        @Test
        @DisplayName("프로필 조회 성공 - isMe = true")
        void getProfile_Me_Success() {
            //given
            given(memberService.findMemberById(memberId)).willReturn(member);
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
            given(memberService.findMemberById(memberId)).willReturn(member);
            given(memberService.findMemberById(requesterId)).willReturn(requester);
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
            given(memberService.findMemberById(memberId))
                    .willThrow(new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> profileService.getProfile(requesterId, memberId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            verify(memberService, never()).findMemberById(requesterId);
            verifyNoInteractions(profileRepository, followRepository);

        }
    }

}