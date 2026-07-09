package com.example.sprout.domain.comment.service;

import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.exception.CommentErrorCode;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceDeleteTest {

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
    private Comment comment;

    @BeforeEach
    void setUp() {

        requesterId = 1L;
        commentId = 100L;

        requester = Member.builder()
                .oauthId("12345")
                .oauthProvider(OauthProvider.KAKAO)
                .build();

        ReflectionTestUtils.setField(requester, "id", requesterId);

        Post post = mock(Post.class);

        comment = Comment.builder()
                .author(requester)
                .post(post)
                .content("댓글")
                .parent(null)
                .build();

        ReflectionTestUtils.setField(comment, "id", commentId);
    }

    @Nested
    @DisplayName("댓글 삭제 성공")
    class Success {

        @Test
        @DisplayName("작성자가 삭제를 요청하면 댓글이 삭제된다")
        void deleteComment_success() {

            given(memberRepository.findById(requesterId))
                    .willReturn(Optional.of(requester));

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.of(comment));

            commentService.deleteComment(requesterId, commentId);

            verify(commentRepository).delete(comment);
        }
    }

    @Nested
    @DisplayName("댓글 삭제 실패")
    class Failure {

        @Test
        @DisplayName("회원이 존재하지 않으면 MEMBER_NOT_FOUND")
        void deleteComment_memberNotFound() {

            given(memberRepository.findById(requesterId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.deleteComment(requesterId, commentId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e ->
                            assertThat(((BusinessException) e).getErrorCode())
                                    .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));

            verify(commentRepository, never()).findById(any());
            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("댓글이 존재하지 않으면 COMMENT_NOT_FOUND")
        void deleteComment_commentNotFound() {

            given(memberRepository.findById(requesterId))
                    .willReturn(Optional.of(requester));

            given(commentRepository.findById(commentId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    commentService.deleteComment(requesterId, commentId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e ->
                            assertThat(((BusinessException) e).getErrorCode())
                                    .isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND));

            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("작성자가 아니면 COMMENT_ACCESS_DENIED")
        void deleteComment_notAuthor() {

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
                    commentService.deleteComment(requesterId, commentId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e ->
                            assertThat(((BusinessException) e).getErrorCode())
                                    .isEqualTo(CommentErrorCode.COMMENT_ACCESS_DENIED));

            verify(commentRepository, never()).delete(any());
        }
    }
}
