package com.example.sprout.domain.comment.service;

import com.example.sprout.domain.comment.dto.request.UpdateCommentRequest;
import com.example.sprout.domain.comment.dto.response.CommentResponse;
import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.exception.CommentErrorCode;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.file.service.S3PresignedUrlService;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.profile.entity.Profile;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceUpdateTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private S3PresignedUrlService s3PresignedUrlService;

    @InjectMocks
    private CommentService commentService;

    private Long requesterId;
    private Long otherMemberId;
    private Long commentId;

    private Member requester;
    private Member otherMember;
    private Profile profile;
    private Post post;
    private Comment comment;
    private UpdateCommentRequest request;

    @BeforeEach
    void setUp() {

        requesterId = 1L;
        otherMemberId = 2L;
        commentId = 100L;

        requester = Member.builder()
                .oauthId("12345")
                .oauthProvider(OauthProvider.KAKAO)
                .build();
        ReflectionTestUtils.setField(requester, "id", requesterId);

        otherMember = Member.builder()
                .oauthId("23456")
                .oauthProvider(OauthProvider.KAKAO)
                .build();
        ReflectionTestUtils.setField(otherMember, "id", otherMemberId);

        profile = Profile.builder()
                .member(requester)
                .nickname("테스터")
                .profileImage("image.png")
                .bio("bio")
                .build();

        post = mock(Post.class);
        lenient().when(post.isPrivate()).thenReturn(false);
        lenient().when(post.getAuthor()).thenReturn(requester);

        comment = Comment.builder()
                .author(requester)
                .post(post)
                .parent(null)
                .content("기존 댓글")
                .isPrivate(false)
                .build();
        ReflectionTestUtils.setField(comment, "id", commentId);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(comment, "updatedAt", LocalDateTime.now());

        request = new UpdateCommentRequest("수정된 댓글", false);
    }

    @Nested
    @DisplayName("댓글 수정 성공")
    class Success {

        @Test
        @DisplayName("작성자 본인이 요청하면 댓글 내용이 수정된다")
        void updateComment_success() {

            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
            given(profileRepository.findByMember(requester)).willReturn(Optional.of(profile));
            given(s3PresignedUrlService.createDownloadUrlOrNull("image.png"))
                    .willReturn("https://presigned-url.example.com/image.png");  // 추가

            CommentResponse response = commentService.updateComment(requesterId, commentId, request);

            assertThat(comment.getContent()).isEqualTo("수정된 댓글");
            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo("수정된 댓글");
            assertThat(response.author().memberId()).isEqualTo(requesterId);
            assertThat(response.author().nickname()).isEqualTo("테스터");
            assertThat(response.author().profileImage())
                    .isEqualTo("https://presigned-url.example.com/image.png");  // 기대값 변경
        }
    }

    @Nested
    @DisplayName("댓글 수정 실패")
    class Failure {

        @Test
        @DisplayName("요청자가 존재하지 않으면 MEMBER_NOT_FOUND")
        void updateComment_memberNotFound() {

            given(memberRepository.findById(requesterId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.updateComment(requesterId, commentId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e ->
                            assertThat(((BusinessException) e).getErrorCode())
                                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));

            verify(commentRepository, never()).findById(any());
        }

        @Test
        @DisplayName("댓글이 존재하지 않으면 COMMENT_NOT_FOUND")
        void updateComment_commentNotFound() {

            given(memberRepository.findById(requesterId))
                    .willReturn(Optional.of(requester));

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.updateComment(requesterId, commentId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e ->
                            assertThat(((BusinessException) e).getErrorCode())
                                    .isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND));
        }

        @Test
        @DisplayName("비공개 게시글에서 게시글 작성자가 아니면 COMMENT_ACCESS_DENIED")
        void updateComment_privatePost_notPostAuthor() {
            given(post.isPrivate()).willReturn(true);
            given(post.getAuthor()).willReturn(mock(Member.class)); // requester와 다른 작성자

            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.updateComment(requesterId, commentId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(CommentErrorCode.COMMENT_ACCESS_DENIED));

            verify(profileRepository, never()).findByMember(any());
        }

        @Test
        @DisplayName("작성자가 아니면 COMMENT_ACCESS_DENIED")
        void updateComment_notAuthor() {

            Member other = Member.builder()
                    .oauthId("99999")
                    .oauthProvider(OauthProvider.KAKAO)
                    .build();

            ReflectionTestUtils.setField(other, "id", 999L);

            given(memberRepository.findById(requesterId))
                    .willReturn(Optional.of(other));

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));

            assertThatThrownBy(() ->
                    commentService.updateComment(requesterId, commentId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e ->
                            assertThat(((BusinessException) e).getErrorCode())
                                    .isEqualTo(CommentErrorCode.COMMENT_ACCESS_DENIED));
        }

        @Test
        @DisplayName("삭제된 댓글은 수정할 수 없다")
        void updateComment_alreadyDeleted() {
            comment.delete();

            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.updateComment(requesterId, commentId, request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(CommentErrorCode.ALREADY_DELETED_COMMENT));
        }

        @Test
        @DisplayName("부모가 비공개면 자식이 공개로 저장돼 있어도 타인에게는 보이지 않는다")
        void isVisible_parentPrivate_childStoredPublic() {
            Long viewerId = 999L;
            Long postAuthorId = requesterId;

            Comment parent = Comment.builder()
                    .author(otherMember).post(post).parent(null)
                    .content("부모").isPrivate(true).build();
            Comment child = Comment.builder()
                    .author(otherMember).post(post).parent(parent)
                    .content("자식").isPrivate(false).build(); // 자식 자체는 공개로 저장

            boolean visible = child.isVisible(viewerId, postAuthorId); // 제3자
            assertThat(visible).isFalse(); // 부모가 비공개라 여전히 안 보임
        }

        @Test
        @DisplayName("부모가 다시 공개로 바뀌면 자식의 원래 공개 설정이 그대로 복원된다")
        void isVisible_parentBackToPublic_childRestoresOriginalSetting() {
            Long viewerId = 999L;
            Long postAuthorId = requesterId;

            Comment parent = Comment.builder()
                    .author(otherMember).post(post).parent(null)
                    .content("부모").isPrivate(false).build(); // 다시 공개로
            Comment child = Comment.builder()
                    .author(otherMember).post(post).parent(parent)
                    .content("자식").isPrivate(false).build(); // 원래 공개로 저장돼 있던 값

            boolean visible = child.isVisible(viewerId, postAuthorId);
            assertThat(visible).isTrue(); // 별도 로직 없이 자연스럽게 복원됨
        }
    }
}