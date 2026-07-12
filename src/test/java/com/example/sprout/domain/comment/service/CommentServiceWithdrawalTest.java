package com.example.sprout.domain.comment.service;

import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.post.entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceWithdrawalTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Long memberId;

    @BeforeEach
    void setUp() {
        memberId = 1L;
    }

    @Nested
    @DisplayName("회원 탈퇴로 인한 댓글 일괄 삭제")
    class SoftDeleteAllByAuthor {

        @Test
        @DisplayName("작성한 댓글이 있으면 모두 soft delete 처리된다")
        void softDeleteAllByAuthor_success() {
            // given
            Post post = mock(Post.class);

            Comment comment1 = Comment.builder()
                    .content("댓글1")
                    .post(post)
                    .build();
            Comment comment2 = Comment.builder()
                    .content("댓글2")
                    .post(post)
                    .build();

            given(commentRepository.findAllByAuthorId(memberId))
                    .willReturn(List.of(comment1, comment2));

            // when
            commentService.softDeleteAllByAuthor(memberId);

            // then
            assertThat(comment1.isDeleted()).isTrue();
            assertThat(comment2.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("작성한 댓글이 없으면 예외 없이 아무 일도 일어나지 않는다")
        void softDeleteAllByAuthor_noComments() {
            // given
            given(commentRepository.findAllByAuthorId(memberId))
                    .willReturn(List.of());

            // when & then (예외 없이 통과하면 성공)
            commentService.softDeleteAllByAuthor(memberId);
        }
    }
}