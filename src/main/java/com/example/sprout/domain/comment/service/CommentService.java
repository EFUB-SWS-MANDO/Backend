package com.example.sprout.domain.comment.service;

import com.example.sprout.domain.comment.dto.request.CreateCommentRequest;
import com.example.sprout.domain.comment.dto.response.CommentResponse;
import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.exception.CommentErrorCode;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
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

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 댓글 생성
    @Transactional
    public CommentResponse createComment(Long requesterId, Long postId, CreateCommentRequest request) {
        log.info("댓글 생성 요청 - postId: {}, parentId: {}", postId, request.parentId());

        Member author = getMember(requesterId);

        Profile authorProfile = getProfile(author);

        Post post = getPost(postId);

        Comment parent = resolveParent(request.parentId(), postId);

        Comment newComment = request.toEntity(author, post, parent);
        commentRepository.save(newComment);

        log.info("댓글 생성 성공");

        return CommentResponse.of(newComment, authorProfile);
    }

    // Helper 함수

    // Member 조회
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 회원 - memberId: {}", memberId);
                    return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
    }

    // Post 조회
    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 게시글 - postId: {}", postId);
                    return new BusinessException(PostErrorCode.POST_NOT_FOUND);
                });
    }

    // Profile 조회
    private Profile getProfile(Member member) {
        return profileRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 프로필 - memberId: {}", member.getId());
                    return new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND);
                });
    }

    // parent 댓글 확정 (null / parentId)
    private Comment resolveParent(Long parentId, Long postId) {
        if (parentId == null) {
            return null;
        }

        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 댓글 - commentId: {}", parentId);
                    return new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
                });
        // parent가 post에 속하는지 확인
        validateParentBelongsToPost(parent, postId);
        return parent;
    }

    // parent 댓글이 게시글에 포함되는지 확인
    private void validateParentBelongsToPost(Comment parent, Long postId) {
        if (!parent.getPost().getId().equals(postId)) {
            log.error("parent가 해당 게시글에 속하지 않습니다. - parentPostId: {}, postId: {}", parent.getPost().getId(), postId);
            throw new BusinessException(CommentErrorCode.PARENT_NOT_IN_POST);
        }
    }
}