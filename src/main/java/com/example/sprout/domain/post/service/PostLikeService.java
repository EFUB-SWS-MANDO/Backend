package com.example.sprout.domain.post.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.dto.response.PostLikeResponse;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostLike;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.post.exception.PostLikeErrorCode;
import com.example.sprout.domain.post.repository.PostLikeRepository;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    @Transactional
    public PostLikeResponse createPostLike(Long memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = getPostById(postId);

        if (postLikeRepository.existsByMemberAndPost(member, post)) {
            log.warn("이미 좋아요를 누른 글의 게시글 좋아요 생성 요청 - memberId: {}, postId: {}", memberId, postId);
            throw new BusinessException(PostLikeErrorCode.POST_LIKE_ALREADY_EXISTS);
        }

        PostLike newPostLike = new PostLike(post, member);
        try {
            postLikeRepository.saveAndFlush(newPostLike);
        } catch (DataIntegrityViolationException e) {
            log.warn("동시 좋아요 요청 감지 - memberId: {}, postId: {}", memberId, postId);
            throw new BusinessException(PostLikeErrorCode.POST_LIKE_ALREADY_EXISTS);
        }
        postRepository.incrementLikeCount(postId);

        int likeCount = postRepository.findLikeCountById(postId);
        return new PostLikeResponse(postId, likeCount);
    }

    @Transactional
    public PostLikeResponse deletePostLike(Long memberId, Long postId) {
        Member member = getMemberById(memberId);
        Post post = getPostById(postId);

        //좋아요 삭제
        long deleted = postLikeRepository.deleteByMemberAndPost(member, post);
        if (deleted == 0) {
            log.warn("좋아요를 누르지 않은 게시글의 좋아요 삭제 요청 - memberId: {}, postId: {}", memberId, postId);
            throw new BusinessException(PostLikeErrorCode.POST_LIKE_NOT_FOUND);
        }
        postRepository.decrementLikeCount(postId);

        int likeCount = postRepository.findLikeCountById(postId);
        return new PostLikeResponse(postId, likeCount);
    }

    @Transactional
    public void deleteByMember(Member member) {
        postLikeRepository.deleteAllByMember(member);
    }

    @Transactional
    public void deleteByPost(Post post) {
        postLikeRepository.deleteAllByPost(post);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() ->{
                    log.warn("존재하지 않는 회원입니다.");
                    return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
    }

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("게시글이 존재하지 않습니다.");
                    return new BusinessException(PostErrorCode.POST_NOT_FOUND);
                });
    }
}
