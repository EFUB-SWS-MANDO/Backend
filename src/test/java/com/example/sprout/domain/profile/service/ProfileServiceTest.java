package com.example.sprout.domain.profile.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.service.MemberService;
import com.example.sprout.domain.profile.dto.request.CreateProfileRequest;
import com.example.sprout.domain.profile.dto.response.CreateProfileResponse;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private MemberService memberService;
    @InjectMocks
    private ProfileService profileService;

    Long memberId;
    Member member;
    CreateProfileRequest request;

    @BeforeEach
    void setUp() {
        memberId = 1L;

        member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", memberId);

        request = new CreateProfileRequest("nickname","profile.png", "bio");
    }

    @Test
    @DisplayName("프로필 생성 성공")
    void  createProfile_Success() {
        //given
        given(memberService.getMemberById(memberId)).willReturn(member);
        //아직 프로필 생성X
        given(profileRepository.existsByMember(member)).willReturn(false);
        given(profileRepository.save(any(Profile.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        //when
        CreateProfileResponse response = profileService.createProfile(memberId, request);

        //then
        assertThat(response).isNotNull();

        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(captor.capture());
        Profile savedProfile = captor.getValue();

        assertThat(savedProfile.getMember()).isEqualTo(member);
        assertThat(savedProfile.getNickname()).isEqualTo(request.nickname());
        assertThat(savedProfile.getProfileImage()).isEqualTo(request.profileImage());
        assertThat(savedProfile.getBio()).isEqualTo(request.bio());

        verify(memberService).getMemberById(memberId);
        verify(profileRepository).existsByMember(member);
    }

    @Test
    @DisplayName("이미 프로필이 존재하는 회원이 프로필 생성 시도 시 실패")
    void createProfile_AlreadyExists_Fail() {
        //given
        given(memberService.getMemberById(memberId)).willReturn(member);
        given(profileRepository.existsByMember(member)).willReturn(true);

        //when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> profileService.createProfile(memberId, request)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ProfileErrorCode.PROFILE_ALREADY_EXISTS);
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원이 프로필 생성 시도 시 실패")
    void createProfile_MemberNotFound_Fail() {
        //given
        given(memberService.getMemberById(memberId))
                .willThrow(new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        //when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> profileService.createProfile(memberId, request)
        );
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
        verify(profileRepository, never()).existsByMember(any());
        verify(profileRepository, never()).save(any(Profile.class));
    }
}
