package com.example.sprout.domain.comment.service;

import com.example.sprout.domain.comment.dto.response.CommentListItemResponse;
import com.example.sprout.domain.comment.dto.response.GetCommentListResponse;
import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.comment.exception.CommentErrorCode;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
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
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CommentServiceGetListTest {

    @Mock private MemberRepository memberRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Long postId;
    private Long viewerId;

    private Member viewer;      // 조회 요청자 (parent2 작성자)
    private Member postAuthor;  // 게시글 작성자
    private Member otherMember; // 제3자 (parent1, child1, parent3 작성자)

    private Post post;

    @BeforeEach
    void setUp() {
        postId = 1L;
        viewerId = 20L;

        viewer = member(20L, "v-oauth");
        postAuthor = member(10L, "p-oauth");
        otherMember = member(30L, "o-oauth");

        post = mock(Post.class);
        lenient().when(post.isPrivate()).thenReturn(false);
        lenient().when(post.getAuthor()).thenReturn(postAuthor);

        given(memberRepository.findById(viewerId)).willReturn(Optional.of(viewer));
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
    }

    private Member member(Long id, String oauthId) {
        Member m = Member.builder().oauthId(oauthId).oauthProvider(OauthProvider.KAKAO).build();
        ReflectionTestUtils.setField(m, "id", id);
        return m;
    }

    private Profile profileOf(Member member, String nickname) {
        return Profile.builder().member(member).nickname(nickname).profileImage("img.png").bio("bio").build();
    }

    @Nested
    @DisplayName("댓글 목록 조회 성공")
    class Success {

        @Test
        @DisplayName("공개/비공개/삭제 댓글이 뷰어 기준으로 올바르게 마스킹된다")
        void getCommentList_visibilityMasking() {
            // parent1(공개, 작성자=otherMember) - child1(비공개, 작성자=otherMember)
            Comment parent1 = Comment.builder()
                    .author(otherMember).post(post).parent(null)
                    .content("공개 부모").isPrivate(false).build();
            ReflectionTestUtils.setField(parent1, "id", 100L);

            Comment child1 = Comment.builder()
                    .author(otherMember).post(post).parent(parent1)
                    .content("비공개 대댓글").isPrivate(true).build();
            ReflectionTestUtils.setField(child1, "id", 101L);

            // parent2(비공개, 작성자=viewer 본인)
            Comment parent2 = Comment.builder()
                    .author(viewer).post(post).parent(null)
                    .content("비공개 본인 댓글").isPrivate(true).build();
            ReflectionTestUtils.setField(parent2, "id", 102L);

            // parent3(삭제됨, 공개였음, 작성자=otherMember)
            Comment parent3 = Comment.builder()
                    .author(otherMember).post(post).parent(null)
                    .content("원래 내용").isPrivate(false).build();
            ReflectionTestUtils.setField(parent3, "id", 103L);
            parent3.delete();

            List<Comment> parents = List.of(parent1, parent2, parent3);

            given(commentRepository.findParentCommentsByPostId(eq(postId), isNull(), any(Pageable.class)))
                    .willReturn(parents);
            given(commentRepository.findChildrenByThreadRootIds(List.of(100L, 102L, 103L)))
                    .willReturn(List.of(child1));
            given(commentRepository.countByPostIdAndParentIsNull(postId)).willReturn(3L);
            given(profileRepository.findByMemberIn(anyList()))
                    .willReturn(List.of(profileOf(viewer, "나"), profileOf(otherMember, "타인")));

            GetCommentListResponse response = commentService.getCommentList(viewerId, postId, null, 10);

            List<CommentListItemResponse> items = response.comments();
            assertThat(items).hasSize(4);

            var parent1Item = findById(items, 100L);
            assertThat(parent1Item.commentResponse().content()).isEqualTo("공개 부모");
            assertThat(parent1Item.hasChildren()).isTrue();

            var child1Item = findById(items, 101L);
            assertThat(child1Item.commentResponse().content()).isEqualTo("비밀댓글입니다.");
            assertThat(child1Item.commentResponse().author().nickname()).isEqualTo("알 수 없음");

            var parent2Item = findById(items, 102L);
            assertThat(parent2Item.commentResponse().content()).isEqualTo("비공개 본인 댓글");
            assertThat(parent2Item.commentResponse().author().nickname()).isEqualTo("나");

            var parent3Item = findById(items, 103L);
            assertThat(parent3Item.commentResponse().content()).isEqualTo("삭제된 댓글입니다");
            assertThat(parent3Item.commentResponse().author().nickname()).isEqualTo("알 수 없음");

            assertThat(response.hasNext()).isFalse();
            assertThat(response.totalElements()).isEqualTo(3L);
        }

        @Test
        @DisplayName("최상위 댓글이 limit+1개 조회되면 hasNext가 true다")
        void getCommentList_hasNext() {
            List<Comment> parents = List.of(
                    buildParent(200L, otherMember),
                    buildParent(201L, otherMember),
                    buildParent(202L, otherMember) // limit=2 -> +1개
            );

            given(commentRepository.findParentCommentsByPostId(eq(postId), isNull(), any(Pageable.class)))
                    .willReturn(parents);
            given(commentRepository.findChildrenByThreadRootIds(List.of(200L, 201L)))
                    .willReturn(List.of());
            given(commentRepository.countByPostIdAndParentIsNull(postId)).willReturn(10L);
            given(profileRepository.findByMemberIn(anyList()))
                    .willReturn(List.of(profileOf(otherMember, "타인")));

            GetCommentListResponse response = commentService.getCommentList(viewerId, postId, null, 2);

            assertThat(response.comments()).hasSize(2); // limit만큼만 반환
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextIdAfter()).isEqualTo(201L);
        }

        private Comment buildParent(Long id, Member author) {
            Comment c = Comment.builder().author(author).post(post).parent(null)
                    .content("댓글" + id).isPrivate(false).build();
            ReflectionTestUtils.setField(c, "id", id);
            return c;
        }

        private CommentListItemResponse findById(List<CommentListItemResponse> items, Long id) {
            return items.stream()
                    .filter(i -> i.commentResponse().commentId().equals(id))
                    .findFirst()
                    .orElseThrow();
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회 실패")
    class Failure {

        @Test
        @DisplayName("비공개 게시글은 게시글 작성자가 아니면 조회할 수 없다")
        void getCommentList_privatePost_notPostAuthor() {
            given(post.isPrivate()).willReturn(true);

            assertThatThrownBy(() -> commentService.getCommentList(viewerId, postId, null, 10))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(CommentErrorCode.COMMENT_ACCESS_DENIED));
        }
    }
}