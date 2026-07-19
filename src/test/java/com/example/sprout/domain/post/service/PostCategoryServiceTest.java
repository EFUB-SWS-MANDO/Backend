package com.example.sprout.domain.post.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostCategory;
import com.example.sprout.domain.post.repository.PostCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class PostCategoryServiceTest {

    @Mock
    private PostCategoryRepository postCategoryRepository;

    @InjectMocks
    private PostCategoryService postCategoryService;

    Post post;
    Category category;

    @BeforeEach
    void setUp() {
        post = Post.builder().build();
        ReflectionTestUtils.setField(post,"id",1L);

        category = new Category("LEADERSHIP");
    }

    @Nested
    @DisplayName("게시글에 카테고리 할당")
    class assignPostCategory{

        @Test
        @DisplayName("게시글에 할당 성공 ")
        void assignPostCategory_Success() {
            //when
            postCategoryService.assignPostCategories(post, List.of(category));

            //then
            ArgumentCaptor<PostCategory> captor = ArgumentCaptor.forClass(PostCategory.class);
            verify(postCategoryRepository).save(captor.capture());

            PostCategory savedPostCategory = captor.getValue();
            assertThat(savedPostCategory.getPost()).isEqualTo(post);
            assertThat(savedPostCategory.getCategory()).isEqualTo(category);
        }
    }

    @Nested
    @DisplayName("게시글의 카테고리 전체 삭제")
    class deleteByPost {

        @Test
        @DisplayName("게시글 삭제 시 연관 PostCategory 삭제 성공")
        void deleteByPost_Succes() {
            //when
            postCategoryService.deleteByPost(post);

            //then
            verify(postCategoryRepository).deleteAllByPost(post);
            verifyNoMoreInteractions(postCategoryRepository);
        }
    }
}