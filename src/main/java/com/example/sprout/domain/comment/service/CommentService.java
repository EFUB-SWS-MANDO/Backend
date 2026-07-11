package com.example.sprout.domain.comment.service;

import com.example.sprout.domain.comment.dto.request.CreateCommentRequest;
import com.example.sprout.domain.comment.dto.request.UpdateCommentRequest;
import com.example.sprout.domain.comment.dto.response.CommentListItemResponse;
import com.example.sprout.domain.comment.dto.response.CommentResponse;
import com.example.sprout.domain.comment.dto.response.GetCommentListResponse;
import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.exception.CommentErrorCode;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.sprout.global.common.util.CursorPageUtils.hasNextPage;
import static com.example.sprout.global.common.util.CursorPageUtils.trimToPageSize;
import static com.example.sprout.global.common.util.CursorPageUtils.resolveNextIdAfter;

import java.util.List;

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

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public GetCommentListResponse getCommentList(Long requesterId, Long postId, Long idAfter, int limit) {

        // 멤버 조회
        Member member = getMember(requesterId);
        // 게시글 조회
        Post post = getPost(postId);

        // idAfter가 있으면 그 댓글의 그룹키(threadRootId 혹은 자기 id)를 먼저 계산
        Long lastGroupKey = resolveLastGroupKey(idAfter);

        // limit + 1개 요청 -> 그만큼 반환되면 다음 페이지가 존재한다고 판단
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Comment> commentList = commentRepository.findCommentsByPostIdOrderedByThread(postId, idAfter, lastGroupKey, pageable);

        List<Long> commentIds = commentList.stream().map(Comment::getId).toList();
        Set<Long> parentIdWithChildren = commentIds.isEmpty()
                ? Set.of()
                : new HashSet<>(commentRepository.findParentIdWithChildren(commentIds));

        boolean hasNext = hasNextPage(commentList, limit);

        // 여분으로 받아온 1개는 응답에 포함 x
        List<Comment> pageCommentList = trimToPageSize(commentList, limit, hasNext);

        Map<Long, Profile> profileMap = buildAuthorProfileMap(pageCommentList);

        List<CommentListItemResponse> commentResponseList = toCommentResponseList(pageCommentList, parentIdWithChildren, profileMap);
        Long nextIdAfter = resolveNextIdAfter(pageCommentList, hasNext, Comment::getId);
        Long totalElements = commentRepository.countByPostId(postId);
        log.info("댓글 목록 조회 완료 - 반환 개수: {}, hasNext: {}", commentResponseList.size(), hasNext);

        return GetCommentListResponse.of(commentResponseList, nextIdAfter, hasNext, totalElements);
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long requesterId, Long commentId, UpdateCommentRequest request) {
        // 멤버 조회
        Member requester = getMember(requesterId);
        // 댓글 조회
        Comment comment = getComment(commentId);

        // requester == comment author랑 일치 여부 확인
        validateAuthor(requester, comment);
        validateNotDeleted(comment);

        // 댓글 수정
        comment.updateComment(request.content());

        // 작성자 프로필 조회
        Profile authorProfile = getProfile(requester);

        return CommentResponse.of(comment, authorProfile);
    }

    // 댓글 삭제

    @Transactional
    public void deleteComment(Long requesterId, Long commentId) {
        // 멤버 조회
        Member requester = getMember(requesterId);
        // 댓글 조회
        Comment comment = getComment(commentId);
        // 작성자/요청자 일치 확인
        validateAuthor(requester, comment);

        // 댓글 삭제
        comment.delete();
    }

    // 회원 탈퇴 시 해당 회원이 작성한 모든 댓글을 soft delete 처리
    // Member 도메인의 탈퇴 서비스에서 호출
    @Transactional
    public void softDeleteAllByAuthor(Long memberId) {
        List<Comment> commentList = commentRepository.findAllByAuthorId(memberId);

        if (commentList.isEmpty()) {
            log.info("탈퇴 회원 작성 댓글 없음 - memberId: {}", memberId);
            return;
        }

        commentList.forEach(Comment::delete);
        log.info("탈퇴 회원 작성 댓글 일괄 삭제 완료 - memberId: {}, 처리 개수: {}", memberId, commentList.size());
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

    // Comment 조회
    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 댓글 - commentId: {}", commentId);
                    return new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
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

        // 삭제된 댓글에는 대댓글 불가
        if (parent.isDeleted()) {
            throw new BusinessException(CommentErrorCode.CANNOT_REPLY_TO_DELETED_COMMENT);
        }

        // 대댓글에는 대댓글 불가
        if (parent.getParent() != null) {
            log.error("대댓글에는 답글을 달 수 없습니다 - parentId: {}", parentId);
            throw new BusinessException(CommentErrorCode.CANNOT_REPLY_TO_REPLY);
        }

        return parent;
    }

    // comment -> CommentResponseList
    private List<CommentListItemResponse> toCommentResponseList(List<Comment> commentList, Set<Long> parentIdWithChildren, Map<Long, Profile> profileMap) {
        return commentList.stream()
                .map(comment -> toCommentResponse(comment, parentIdWithChildren.contains(comment.getId()), profileMap))
                .toList();
    }

    // comment -> CommentResponse 변환
    private CommentListItemResponse toCommentResponse(Comment comment, boolean hasChildren, Map<Long, Profile> profileMap) {
        Member author = comment.getAuthor();
        Profile authorProfile = (author != null)
                ? profileMap.get(author.getId()) : null;

        return CommentListItemResponse.of(comment, authorProfile, hasChildren);
    }

    private Map<Long, Profile> buildAuthorProfileMap(List<Comment> commentList) {
        List<Member> authorList = commentList.stream()
                .map(Comment::getAuthor)
                .filter(Objects::nonNull)  // 탈퇴 회원 제외
                .distinct()
                .toList();

        if (authorList.isEmpty()) {
            return Map.of();
        }

        return profileRepository.findByMemberIn(authorList).stream()
                .collect(Collectors.toMap(p -> p.getMember().getId(), p -> p));
    }

    // parent 댓글이 게시글에 포함되는지 확인
    private void validateParentBelongsToPost(Comment parent, Long postId) {
        if (!parent.getPost().getId().equals(postId)) {
            log.error("parent가 해당 게시글에 속하지 않습니다. - parentPostId: {}, postId: {}", parent.getPost().getId(), postId);
            throw new BusinessException(CommentErrorCode.PARENT_NOT_IN_POST);
        }
    }

    // idAfter 댓글의 그룹키(threadRootId ?? id) 계산
    private Long resolveLastGroupKey(Long idAfter) {
        if (idAfter == null) {
            return null;
        }
        Comment lastComment = getComment(idAfter);
        return lastComment.getThreadRootId() != null ? lastComment.getThreadRootId() : lastComment.getId();
    }

    // 댓글 작성자/요청자 일치 확인
    private void validateAuthor(Member member, Comment comment) {
        if (comment.getAuthor() == null || !comment.isAuthor(member)) {
            Long authorId = (comment.getAuthor() != null) ? comment.getAuthor().getId() : null;
            log.error("댓글 작성자가 아닙니다. - memberId, authorId: {}, {}", member.getId(), authorId);
            throw new BusinessException(CommentErrorCode.COMMENT_ACCESS_DENIED);
        }
    }

    // 삭제된 댓글인지 확인
    private void validateNotDeleted(Comment comment) {
        if (comment.isDeleted()) {
            log.error("이미 삭제된 댓글은 수정할 수 없습니다 - commentId: {}", comment.getId());
            throw new BusinessException(CommentErrorCode.ALREADY_DELETED_COMMENT);
        }
    }
}