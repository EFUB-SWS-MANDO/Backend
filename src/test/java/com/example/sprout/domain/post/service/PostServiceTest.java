package com.example.sprout.domain.post.service;

import com.example.sprout.domain.comment.service.CommentService;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentService commentService;
    @Mock
    private PostLikeService postLikeService;
    @Mock
    private PostCategoryService postCategoryService;

    @InjectMocks
    private PostService postService;

    Member member;
    Post post1;
    Post post2;

    @BeforeEach
    void setUp() {
        member = Member.builder().build();
        ReflectionTestUtils.setField(member,"id", 1L);

        post1 = Post.builder().build();
        ReflectionTestUtils.setField(post1, "id", 10L);

        post2 = Post.builder().build();
        ReflectionTestUtils.setField(post2,"id", 20L);
    }

    @Test
    @DisplayName("회원의 게시글이 여러 건일 때 게시글마다 자식 엔티티 먼저 삭제 후, 게시글 삭제")
    void deletePostByMember_MultiplePosts_Success() {
        //given
        given(postRepository.findAllByAuthor(member)).willReturn(List.of(post1, post2));

        //when
        postService.deletePostByMember(member);

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
        given(postRepository.findAllByAuthor(member)).willReturn(Collections.emptyList());

        //when
        postService.deletePostByMember(member);

        //then
        verifyNoInteractions(commentService, postLikeService, postCategoryService);
    }
}