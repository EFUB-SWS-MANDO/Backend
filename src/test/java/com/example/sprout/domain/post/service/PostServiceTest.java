package com.example.sprout.domain.post.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.exception.CategoryErrorCode;
import com.example.sprout.domain.category.repository.CategoryRepository;
import com.example.sprout.domain.comment.service.CommentService;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.dto.request.CreatePostRequest;
import com.example.sprout.domain.post.dto.request.UpdatePostRequest;
import com.example.sprout.domain.post.dto.response.PostDetailDto;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private CommentService commentService;
    @Mock
    private PostLikeService postLikeService;
    @Mock
    private PostCategoryService postCategoryService;

    @InjectMocks
    private PostService postService;

    Long authorId;
    Member author;
    Profile authorProfile;

    @BeforeEach
    void setUp() {
        authorId = 1L;
        author = Member.builder().build();
        ReflectionTestUtils.setField(author,"id", authorId);

        authorProfile = new Profile(author, "nickname", "profile.png", "bio");
    }

    @Nested
    @DisplayName("게시글 생성")
    class createPost {
        CreatePostRequest request;

        @BeforeEach
        void setUp() {
            request = new CreatePostRequest("게시글 제목1", "게시글 내용1", new ArrayList<>(List.of("LEADERSHIP", "COMMUNICATION")));
        }

        @Test
        @DisplayName("게시글 생성 성공 - 카테고리 선택 존재")
        void createPost_Success() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(categoryRepository.findAllByTypeIn(request.categories())).willReturn(List.of(
                    new Category("LEADERSHIP"), new Category("COMMUNICATION")
            ));
            given(profileRepository.findByMember(author)).willReturn(Optional.of(authorProfile));

            //when
           PostDetailDto response = postService.createPost(authorId, request);

            //then
            assertThat(response).isNotNull();
            assertThat(response.author().memberId()).isEqualTo(author.getId());
            assertThat(response.author().nickname()).isEqualTo(authorProfile.getNickname());
            assertThat(response.author().profileImage()).isEqualTo(authorProfile.getProfileImage());
            assertThat(response.author().isFollowing()).isFalse();
            assertThat(response.title()).isEqualTo(request.title());
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.isMine()).isTrue();
            assertThat(response.likeCount()).isEqualTo(0);

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(captor.capture());
            Post savedPost = captor.getValue();

            assertThat(savedPost.getAuthor()).isEqualTo(author);
            assertThat(savedPost.getTitle()).isEqualTo(request.title());
            assertThat(savedPost.getContent()).isEqualTo(request.content());
            assertThat(savedPost.getLikeCount()).isEqualTo(0);

            verify(memberRepository).findById(authorId);
            verify(categoryRepository).findAllByTypeIn(request.categories());
            verify(profileRepository).findByMember(author);

            ArgumentCaptor<List<Category>> categoryCaptor = ArgumentCaptor.forClass(List.class);
            verify(postCategoryService).assignPostCategories(eq(savedPost), categoryCaptor.capture());
            assertThat(categoryCaptor.getValue())
                    .extracting(Category::getType)
                    .containsExactly("LEADERSHIP", "COMMUNICATION");

        }

        @Test
        @DisplayName("존재하지 않는 회원이 게시글 생성 요청 시 실패")
        void createPost_MemberNotFound_Fail() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.empty());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.createPost(authorId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            verify(postRepository, never()).save(any(Post.class));
            verify(memberRepository).findById(authorId);
            verifyNoInteractions(categoryRepository, profileRepository, postCategoryService);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 게시글 생성 요청 시 실패 - 전체 카테고리 실패, 빈 리스트 반환")
        void createPost_CategoryNotFound_Fail() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(categoryRepository.findAllByTypeIn(request.categories())).willReturn(List.of());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.createPost(authorId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);

            verify(memberRepository).findById(authorId);
            verify(categoryRepository).findAllByTypeIn(request.categories());

            verify(postRepository, never()).save(any(Post.class));
            verifyNoInteractions(profileRepository);
            verify(postCategoryService, never()).assignPostCategories(any(),any());
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 게시글 생성 요청 시 실패 - 일부 카테고리 조회 실패, 리스트의 크기 다름")
        void createPost_Partial_CategoryNotFound_Fail() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(categoryRepository.findAllByTypeIn(request.categories())).willReturn(List.of( new Category("LEADERSHIP")));

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.createPost(authorId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);

            verify(memberRepository).findById(authorId);
            verify(categoryRepository).findAllByTypeIn(request.categories());

            verify(postRepository, never()).save(any(Post.class));
            verifyNoInteractions(profileRepository);
        }

        @Test
        @DisplayName("존재하지 않는 프로필의 회원이 게시글 생성 요청 시 실패")
        void createPost_ProfileNotFound_Fail() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(categoryRepository.findAllByTypeIn(request.categories())).willReturn(List.of(
                    new Category("LEADERSHIP"), new Category("COMMUNICATION")
            ));
            given(profileRepository.findByMember(author)).willReturn(Optional.empty());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.createPost(authorId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ProfileErrorCode.PROFILE_NOT_FOUND);

            verify(postRepository, never()).save(any(Post.class));
            verifyNoInteractions(postCategoryService);

        }
    }

    @Nested
    @DisplayName("게시글 상세 조회")
    class getPostDetail {

        Long requesterId;
        Member requester;
        Long postId;
        Post post;

        @BeforeEach
        void setUp() {
            requesterId = 2L;
            requester = Member.builder().build();
            ReflectionTestUtils.setField(requester, "id", requesterId);

            postId = 1L;
            post = Post.builder()
                    .author(author)
                    .title("title")
                    .content("content")
                    .build();
            ReflectionTestUtils.setField(post, "id", postId);
        }

        @Test
        @DisplayName("게시글 상세조회 성공 - 타인 글 조회, isFollowing = false")
        void getPostDetail_NotFollowing_Success() {
            //given
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(profileRepository.findByMember(author)).willReturn(Optional.of(authorProfile));
            given(followRepository.existsByFollowerIdAndFolloweeId(requesterId, authorId)).willReturn(false);

            //when
            PostDetailDto response = postService.getPostDetail(requesterId, postId);

            //then
            assertThat(response).isNotNull();
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.title()).isEqualTo("title");
            assertThat(response.content()).isEqualTo("content");
            assertThat(response.author().memberId()).isEqualTo(authorId);
            assertThat(response.author().nickname()).isEqualTo(authorProfile.getNickname());
            assertThat(response.author().isFollowing()).isFalse();
            assertThat(response.isMine()).isFalse();

            verify(memberRepository).findById(requesterId);
            verify(postRepository).findById(postId);
            verify(profileRepository).findByMember(author);
            verify(followRepository).existsByFollowerIdAndFolloweeId(requesterId, authorId);
        }

        @Test
        @DisplayName("게시글 상세조회 성공 - 타인 글 조회, isFollowing = O")
        void getPostDetail_Following_Success() {
            //given
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(profileRepository.findByMember(author)).willReturn(Optional.of(authorProfile));
            given(followRepository.existsByFollowerIdAndFolloweeId(requesterId, authorId)).willReturn(true);

            //when
            PostDetailDto response = postService.getPostDetail(requesterId, postId);

            //then
            assertThat(response).isNotNull();
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.title()).isEqualTo("title");
            assertThat(response.content()).isEqualTo("content");
            assertThat(response.author().memberId()).isEqualTo(authorId);
            assertThat(response.author().nickname()).isEqualTo(authorProfile.getNickname());
            assertThat(response.author().isFollowing()).isTrue();
            assertThat(response.isMine()).isFalse();

            verify(memberRepository).findById(requesterId);
            verify(postRepository).findById(postId);
            verify(profileRepository).findByMember(author);
            verify(followRepository).existsByFollowerIdAndFolloweeId(requesterId, authorId);
        }

        @Test
        @DisplayName("게시글 상세조회 성공 - 본인 글 조회")
        void getPostDetail_isMineTrue_Success() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(profileRepository.findByMember(author)).willReturn(Optional.of(authorProfile));

            //when
            PostDetailDto response = postService.getPostDetail(authorId, postId);

            //then
            assertThat(response).isNotNull();
            assertThat(response.postId()).isEqualTo(postId);
            assertThat(response.title()).isEqualTo("title");
            assertThat(response.content()).isEqualTo("content");
            assertThat(response.author().memberId()).isEqualTo(authorId);
            assertThat(response.author().nickname()).isEqualTo(authorProfile.getNickname());
            assertThat(response.author().isFollowing()).isFalse();
            assertThat(response.isMine()).isTrue();

            verify(memberRepository).findById(authorId);
            verify(postRepository).findById(postId);
            verify(profileRepository).findByMember(author);
            verifyNoInteractions(followRepository);
        }

        @Test
        @DisplayName("존재하지 않는 회원이 게시글 조회 요청 시 실패")
        void getPostDetail_MemberNotFound_Fail() {
            //given
            given(memberRepository.findById(requesterId)).willReturn(Optional.empty());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.getPostDetail(requesterId, postId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository).findById(requesterId);
            verifyNoInteractions(postRepository, profileRepository, followRepository);
        }

        @Test
        @DisplayName("존재하지 않는 게시글을 조회하려 시도 시 실패")
        void getPostDetail_PostNotFound_Fail() {
            //given
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(postRepository.findById(postId)).willReturn(Optional.empty());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.getPostDetail(requesterId, postId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(PostErrorCode.POST_NOT_FOUND);

            verify(memberRepository).findById(requesterId);
            verify(postRepository).findById(postId);
            verifyNoInteractions(profileRepository, followRepository);
        }

        @Test
        @DisplayName("존재하지 않는 작성자 프로필의 게시글 조회 요청 시 실패")
        void getPostDetail_ProfileNotFound_Fail() {
            //given
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(profileRepository.findByMember(author)).willReturn(Optional.empty());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.getPostDetail(requesterId, postId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ProfileErrorCode.PROFILE_NOT_FOUND);

            verify(memberRepository).findById(requesterId);
            verify(postRepository).findById(postId);
            verify(profileRepository).findByMember(author);
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class updatePost {
        Long requesterId;
        Member requester;
        Long postId;
        Post post;
        UpdatePostRequest request;

        @BeforeEach
        void setUp() {
            requesterId = 2L;
            requester = Member.builder().build();
            ReflectionTestUtils.setField(requester, "id", requesterId);

            postId = 1L;
            post = Post.builder()
                    .author(author)
                    .title("title")
                    .content("content")
                    .build();
            ReflectionTestUtils.setField(post, "id", postId);

            request = new UpdatePostRequest("update_title", "update_content", List.of("CREATIVITY"));
        }

        @Test
        @DisplayName("게시글 수정 성공")
        void updatePost_Success() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(profileRepository.findByMember(author)).willReturn(Optional.of(authorProfile));
            given(categoryRepository.findAllByTypeIn(request.categories())).willReturn(List.of(new Category("PLANNING")));

            //when
            PostDetailDto response = postService.updatePost(authorId, postId, request);

            //then
            assertThat(response).isNotNull();

            assertThat(response.author().memberId()).isEqualTo(author.getId());
            assertThat(response.author().nickname()).isEqualTo(authorProfile.getNickname());
            assertThat(response.author().profileImage()).isEqualTo(authorProfile.getProfileImage());
            assertThat(response.author().isFollowing()).isFalse();

            assertThat(response.title()).isEqualTo(request.title());
            assertThat(response.content()).isEqualTo(request.content());
            assertThat(response.isMine()).isTrue();

            assertThat(post.getTitle()).isEqualTo("update_title");
            assertThat(post.getContent()).isEqualTo("update_content");

            verify(memberRepository).findById(authorId);
            verify(postRepository).findById(postId);
            verify(profileRepository).findByMember(author);
        }

        @Test
        @DisplayName("존재하지 않는 회원이 게시글 수정 요청 시 실패")
        void updatePost_MemberNotFound_Fail() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.empty());

            //when
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.updatePost(authorId, postId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository).findById(authorId);
            verifyNoInteractions(postRepository, profileRepository, categoryRepository);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정 요청 시 실패")
        void updatePost_PostNotFound_Fail() {
            //given
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(postRepository.findById(postId)).willReturn(Optional.empty());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.updatePost(authorId, postId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(PostErrorCode.POST_NOT_FOUND);

            verify(memberRepository).findById(authorId);
            verify(postRepository).findById(postId);
            verifyNoInteractions(profileRepository, categoryRepository);
        }

        @Test
        @DisplayName("프로필이 존재하지 않는 회원의 게시글 수정 요청 시 실패")
        void updatePost_ProfileNotFound_Fail() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(profileRepository.findByMember(author)).willReturn(Optional.empty());

            //when
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.updatePost(authorId, postId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(ProfileErrorCode.PROFILE_NOT_FOUND);

            verify(memberRepository).findById(authorId);
            verify(postRepository).findById(postId);
            verify(profileRepository).findByMember(author);
            verifyNoInteractions(categoryRepository);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 게시글 수정 요청 시 실패")
        void updatePost_CategoryNotFound_Fail() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));
            given(profileRepository.findByMember(author)).willReturn(Optional.of(authorProfile));
            given(categoryRepository.findAllByTypeIn(request.categories())).willReturn(List.of());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.updatePost(authorId, postId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(CategoryErrorCode.CATEGORY_NOT_FOUND);

            verify(memberRepository).findById(authorId);
            verify(postRepository).findById(postId);
            verify(profileRepository).findByMember(author);
            verify(categoryRepository).findAllByTypeIn(request.categories());
        }

        @Test
        @DisplayName("작성자가 아닌 회원의 게시글 수정 요청 시 실패")
        void updatePost_AccessDenied_Fail() {
            //given
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(requester));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.updatePost(requesterId, postId, request)
            );
            assertThat(exception.getErrorCode()).isEqualTo(PostErrorCode.POST_ACCESS_DENIED);

            verify(memberRepository).findById(requesterId);
            verify(postRepository).findById(postId);
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class deletePost{
        Long memberId;
        Member member;
        Long postId;
        Post post;

        @BeforeEach
        void setUp() {
            memberId = 2L;
            member = Member.builder().build();
            ReflectionTestUtils.setField(member, "id", memberId);

            postId = 1L;
            post = Post.builder()
                    .author(author)
                    .title("title")
                    .content("content")
                    .build();
            ReflectionTestUtils.setField(post, "id", postId);
        }

        @Test
        @DisplayName("게시글 삭제 성공")
        void deletePost_Success() {
            //given
            given(memberRepository.findById(authorId)).willReturn(Optional.of(author));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));

            //when
            postService.deletePostWithChildren(authorId, postId);

            //then: comment -> postLike -> postCategory -> post 순서대로 삭제
            InOrder inOrder = inOrder(commentService, postLikeService, postCategoryService, postRepository);
            inOrder.verify(commentService).deleteByPost(post);
            inOrder.verify(postLikeService).deleteByPost(post);
            inOrder.verify(postCategoryService).deleteByPost(post);
            inOrder.verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("작성자가 아닌 회원이 게시글 삭제 요청 시 실패")
        void deletePost_AccessDenied_Fail() {
            //given
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(postRepository.findById(postId)).willReturn(Optional.of(post));

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.deletePostWithChildren(memberId, postId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(PostErrorCode.POST_ACCESS_DENIED);

            verify(memberRepository).findById(memberId);
            verify(postRepository).findById(postId);
            verify(postRepository, never()).delete(any(Post.class));
            verifyNoInteractions(commentService, postLikeService, postCategoryService);

        }

        @Test
        @DisplayName("존재하지 않는 회원이 게시글 삭제 요청 시 실패")
        void deletePost_MemberNotFound_Fail() {
            //given
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.deletePostWithChildren(memberId, postId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

            verify(memberRepository).findById(memberId);
            verifyNoInteractions(postRepository, commentService, postLikeService, postCategoryService);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 요청 시 실패")
        void deletePost_PostNotFound_Fail() {
            //given
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(postRepository.findById(postId)).willReturn(Optional.empty());

            //when & then
            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> postService.deletePostWithChildren(memberId, postId)
            );
            assertThat(exception.getErrorCode()).isEqualTo(PostErrorCode.POST_NOT_FOUND);

            verify(memberRepository).findById(memberId);
            verify(postRepository).findById(postId);
            verify(postRepository, never()).delete(any(Post.class));
            verifyNoInteractions(commentService, postLikeService, postCategoryService);

        }

    }

    @Nested
    @DisplayName("회원이 작성한 게시글 전체 삭제")
    class deletePostByAuthor {
        Post post1;
        Post post2;

        @BeforeEach
        void setUp() {
            post1 = Post.builder().build();
            ReflectionTestUtils.setField(post1, "id", 10L);

            post2 = Post.builder().build();
            ReflectionTestUtils.setField(post2,"id", 20L);
        }

        @Test
        @DisplayName("회원의 게시글이 여러 건일 때 게시글마다 자식 엔티티 먼저 삭제 후, 게시글 삭제")
        void deletePostByMember_MultiplePosts_Success() {
            //given
            given(postRepository.findAllByAuthor(author)).willReturn(List.of(post1, post2));

            //when
            postService.deletePostByMember(author);

            //then: post1, post2 각각에 대해 comment -> postLike -> postCategory -> post 삭제
            InOrder inOrder = inOrder(commentService, postLikeService, postCategoryService, postRepository);

            inOrder.verify(commentService).deleteByPost(post1);
            inOrder.verify(postLikeService).deleteByPost(post1);
            inOrder.verify(postCategoryService).deleteByPost(post1);
            inOrder.verify(postRepository).delete(post1);

            inOrder.verify(commentService).deleteByPost(post2);
            inOrder.verify(postLikeService).deleteByPost(post2);
            inOrder.verify(postCategoryService).deleteByPost(post2);
            inOrder.verify(postRepository).delete(post2);
        }

        @Test
        @DisplayName("삭제할 게시글이 없으면 엔티티 삭제 로직 호출하지 않음")
        void deletePostByMember_NoPosts_Success() {
            //given
            given(postRepository.findAllByAuthor(author)).willReturn(Collections.emptyList());

            //when
            postService.deletePostByMember(author);

            //then
            verifyNoInteractions(commentService, postLikeService, postCategoryService);
            verify(postRepository, never()).delete(any(Post.class));
        }
    }
}