package com.example.sprout.domain.member.service;

import com.example.sprout.domain.auth.service.AuthService;
import com.example.sprout.domain.comment.service.CommentService;
import com.example.sprout.domain.follow.service.FollowService;
import com.example.sprout.domain.interview.service.InterviewSessionService;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.service.PostLikeService;
import com.example.sprout.domain.post.service.PostService;
import com.example.sprout.domain.profile.service.ProfileService;
import com.example.sprout.domain.resume.service.ResumeService;
import com.example.sprout.global.error.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PostService postService;
    @Mock
    private ResumeService resumeService;
    @Mock
    private InterviewSessionService interviewSessionService;
    @Mock
    private FollowService followService;
    @Mock
    private ProfileService profileService;
    @Mock
    private PostLikeService postLikeService;
    @Mock
    private CommentService commentService;
    @Mock
    private AuthService authService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MemberService memberService;

    Long memberId;
    Member member;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        member = Member.builder().build();
        ReflectionTestUtils.setField(member,"id", memberId);
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - 연관 도메인 삭제 후 회원 삭제")
    void deleteMember_Success() {
        //given
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        //when
        memberService.deleteMember(memberId,request);

        //then: 각 서비스가 올바른 인자로 정확히 1번씩 호출됐는지 확인
        verify(profileService).deleteByMember(member);
        verify(postLikeService).deleteByMember(member);
        verify(postService).deletePostByMember(member);
        verify(commentService).softDeleteAllByAuthor(memberId);
        verify(resumeService).deleteByMember(member);
        verify(interviewSessionService).deleteAllByMember(member);
        verify(followService).deleteFollowByMember(member);
        verify(memberRepository).delete(member);
        verify(authService).signOut(memberId, request);

        //then: FK 의존성 -> 삭제 순서 호출
        InOrder inOrder = inOrder(
                profileService, postLikeService, postService,
                commentService, resumeService, interviewSessionService,
                followService, memberRepository, authService
        );
        inOrder.verify(profileService).deleteByMember(member);
        inOrder.verify(postLikeService).deleteByMember(member);
        inOrder.verify(postService).deletePostByMember(member);
        inOrder.verify(commentService).softDeleteAllByAuthor(memberId);
        inOrder.verify(resumeService).deleteByMember(member);
        inOrder.verify(interviewSessionService).deleteAllByMember(member);
        inOrder.verify(followService).deleteFollowByMember(member);
        inOrder.verify(memberRepository).delete(member);
    }

    @Test
    @DisplayName("존재하지 않는 회원 탈퇴 시도 시 실패 - 자식 도메인 삭제 로직 미실행")
    void deleteMember_NotFound_Fail() {
        //given
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        //when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> memberService.deleteMember(memberId, request)
        );
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

        //회원 못 찾았을 때, 도메인 삭제 로직 호출되면 안됨
        verifyNoInteractions(
                profileService, postLikeService, postService,
                commentService, resumeService, interviewSessionService, followService,
                authService
        );
        verify(memberRepository, never()).delete(member);
    }
}