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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;

    private final AuthService authService;
    private final PostService postService;
    private final ResumeService resumeService;
    private final InterviewSessionService interviewSessionService;
    private final FollowService followService;
    private final ProfileService profileService;
    private final PostLikeService postLikeService;
    private final CommentService commentService;

    @Transactional
    public void deleteMember(Long memberId, HttpServletRequest request) {

        Member member = findById(memberId);

        //Member를 가지는 자식 엔티티 우선 삭제
        //Profile -> Comment -> PostLike -> Post -> Resume -> Interview -> Follow
        profileService.deleteByMember(member);
        postLikeService.deleteByMember(member);
        postService.deletePostByMember(member);
        commentService.softDeleteAllByAuthor(memberId);
        resumeService.deleteByMember(member);
        interviewSessionService.deleteAllByMember(member);
        followService.deleteFollowByMember(member);

        //Member 삭제
        memberRepository.delete(member);
        log.info("회원탈퇴 성공 - memberId: {}", memberId);

        //Member 로그아웃
        authService.signOut(memberId,request);
    }

    private Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.debug("존재하지 않는 회원 - memberId: {}", memberId);
                    return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
    }
}
