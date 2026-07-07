package com.example.sprout.domain.comment.service;

import com.example.sprout.domain.comment.dto.request.CreateCommentRequest;
import com.example.sprout.domain.comment.dto.response.CommentResponse;
import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.exception.CommentErrorCode;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.global.common.response.ApiResponse;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    // 댓글 생성
    @Transactional
    public ApiResponse<CommentResponse> createComment(Long requesterId, Long postId, CreateCommentRequest request) {
        log.info("댓글 생성 요청 - postId: {}, parentId: {}", postId, request.parentId());

        Member author = memberRepository.findByMemberId()
                .orElseThrow(() -> {
                    log.error("존재하지 않는 회원 - memberId: {}", requesterId);
                    return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
        Profile authorProfile = profileRepository.findByMember(author)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 프로필 - memberId: {}", author.getId());
                    return new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND);
                })
        Post post = postRepository.findByPostId()
                .orElseThrow(() -> {
                    log.error("존재하지 않는 게시글 - postId: {}", postId);
                    return new BusinessException(PostErrorCode.POST_NOT_FOUND);
                });
        Comment parent = request.parentId() != null
                ? commentRepository.findById(request.parentId())
                .orElseThrow(() -> {
                    log.error("존재하지 않는 댓글 - commentId: {}", request.parentId());
                    return new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
                });
        Comment newComment = request.toEntity(author, post, parent);
        commentRepository.save(newComment);
        log.info("댓글 생성 성공");

        boolean isFollowing = followRepository.exitsByFollowerIdAndFollowingId(
                author.getId(), newComment.getAuthor().getId()
        );
        return ApiResponse.success("댓글 생성 성공", CommentResponse.of(newComment, authorProfile, isFollowing));
    }
}
