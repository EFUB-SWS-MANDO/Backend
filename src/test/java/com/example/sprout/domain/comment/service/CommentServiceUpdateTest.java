package com.example.sprout.domain.comment.service;

import com.example.sprout.domain.comment.dto.request.UpdateCommentRequest;
import com.example.sprout.domain.comment.dto.response.CommentResponse;
import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.exception.CommentErrorCode;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostRepository;
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
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Long requesterId;
    private Long commentId;

    private Member requester;
    private Profile profile;
    private Comment comment;

    private UpdateCommentRequest request;

    @BeforeEach
    void setUp() {

        requesterId = 1L;
        commentId = 100L;

        requester = Member.builder()
                .oauthId("12345")
                .oauthProvider(OauthProvider.KAKAO)
                .build();
        ReflectionTestUtils.setField(requester, "id", requesterId);

        profile = Profile.builder()
                .member(requester)
                .nickname("테스터")
                .profileImage("image.png")
                .bio("bio")
                .build();

        Post post = mock(Post.class);

        comment = Comment.builder()
                .author(requester)
                .post(post)
                .parent(null)
                .content("기존 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "id", commentId);

        request = new UpdateCommentRequest("수정된 댓글");
    }

    @Nested
    @DisplayName("댓글 수정 성공")
    class Success {

        @Test
        @DisplayName("작성자 본인이 요청하면 댓글 내용이 수정된다")
        void updateComment_success() {

            given(memberRepository.findById(requesterId))
                    .willReturn(Optional.of(requester));

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));

            given(profileRepository.findByMember(requester))
                    .willReturn(Optional.of(profile));

            CommentResponse response =
                    commentService.updateComment(requesterId, commentId, request);

            assertThat(comment.getContent()).isEqualTo("수정된 댓글");

            assertThat(response.commentId()).isEqualTo(commentId);
            assertThat(response.content()).isEqualTo("수정된 댓글");

            assertThat(response.author().memberId()).isEqualTo(requesterId);
            assertThat(response.author().nickname()).isEqualTo("테스터");
            assertThat(response.author().profileImage()).isEqualTo("image.png");
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
    }
}