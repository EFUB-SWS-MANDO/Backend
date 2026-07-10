package com.example.sprout.follow;

import com.example.sprout.domain.follow.dto.response.FollowCreateResponse;
import com.example.sprout.domain.follow.entity.Follow;
import com.example.sprout.domain.follow.exception.FollowErrorCode;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.follow.service.FollowService;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.global.error.BusinessException;
import com.example.sprout.global.error.GlobalErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class FollowServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowService followService;

    Long requesterId;
    Long followeeId;
    Member requester;
    Member followee;

    @BeforeEach
    void setUp() {
        requesterId = 1L;
        followeeId = 2L;

        requester = Member.builder().build();
        ReflectionTestUtils.setField(requester, "id",  requesterId);
        followee = Member.builder().build();
        ReflectionTestUtils.setField(followee, "id",  followeeId);
    }


    @Test
    @DisplayName("팔로우 생성 성공")
    void createFollow_Success() {
        // given
        given(memberRepository.existsById(requesterId)).willReturn(true);
        given(memberRepository.existsById(followeeId)).willReturn(true);
        given(memberRepository.getReferenceById(requesterId)).willReturn(requester);
        given(memberRepository.getReferenceById(followeeId)).willReturn(followee);

        given(followRepository.save(any(Follow.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        FollowCreateResponse response = followService.createFollow(requesterId, followeeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.followerId()).isEqualTo(requesterId);
        assertThat(response.followeeId()).isEqualTo(followeeId);
        verify(memberRepository).existsById(requesterId);
        verify(memberRepository).existsById(followeeId);
        verify(memberRepository).getReferenceById(requesterId);
        verify(memberRepository).getReferenceById(followeeId);
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저로 팔로우 시도 시 실패")
    void createFollow_NotFoundMember_Fail() {
        // given
        given(memberRepository.existsById(requesterId)).willReturn(true);
        given(memberRepository.existsById(followeeId)).willReturn(false);

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> followService.createFollow(requesterId, followeeId)
        );
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("이미 팔로우하는 유저를 팔로우 시도 시 실패")
    void createFollow_AlreadyFollowedMember_Fail() {
        // given
        given(memberRepository.existsById(requesterId)).willReturn(true);
        given(memberRepository.existsById(followeeId)).willReturn(true);
        given(memberRepository.getReferenceById(requesterId)).willReturn(requester);
        given(memberRepository.getReferenceById(followeeId)).willReturn(followee);

        given(followRepository.save(any(Follow.class)))
                .willThrow(DataIntegrityViolationException.class);

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> followService.createFollow(requesterId, followeeId)
        );
        assertThat(exception.getErrorCode()).isEqualTo(FollowErrorCode.FOLLOW_ALREADY_EXISTS);
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("자기 자신 팔로우 시도 시 실패")
    void createFollow_FollowSelf_Fail() {
        // given
        requesterId = 1L;
        followeeId = 1L;

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> followService.createFollow(requesterId, followeeId)
        );
        assertThat(exception.getErrorCode()).isEqualTo(FollowErrorCode.CANNOT_FOLLOW_SELF);
        verify(followRepository, never()).save(any(Follow.class));
    }


    @Test
    @DisplayName("팔로우 취소 성공")
    void deleteFollow_Success() {
        // given
        Follow follow = Follow.builder().follower(requester).followee(followee).build();
        given(followRepository.deleteByFollowerIdAndFolloweeId(requesterId, followeeId))
                .willReturn(1);

        // when
        followService.deleteFollow(requesterId, followeeId);

        // then
        verify(followRepository).deleteByFollowerIdAndFolloweeId(requesterId, followeeId);
    }

    @Test
    @DisplayName("팔로우하지 않는 유저를 팔로우 취소 시도 시 실패")
    void deleteFollow_NotFollowedMember_Fail() {
        // given
        given(followRepository.deleteByFollowerIdAndFolloweeId(requesterId, followeeId))
                .willReturn(0);

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> followService.deleteFollow(requesterId, followeeId)
        );
        assertThat(exception.getErrorCode()).isEqualTo(FollowErrorCode.FOLLOW_NOT_FOUND);
        verify(followRepository).deleteByFollowerIdAndFolloweeId(requesterId, followeeId);
    }

}
