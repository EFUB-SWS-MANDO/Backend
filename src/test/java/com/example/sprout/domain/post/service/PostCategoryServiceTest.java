package com.example.sprout.domain.post.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.member.entity.Member;
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
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.N;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
        void assignPostCategories_Success() {
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

    @Nested
    @DisplayName("카테고리 수정 (diff)")
    class updatePostCategories {

        Category categoryA;
        Category categoryB;
        Category categoryC;

        @BeforeEach
        void setUp() {
            Member author = Member.builder().build();
            ReflectionTestUtils.setField(author, "id", 1L);

            post = Post.builder()
                    .author(author)
                    .title("title")
                    .content("content").build();

            categoryA = category(10L, "COLLABORATION");
            categoryB = category(20L, "GROWTH");
            categoryC = category(30L, "LEADERSHIP");
        }

        @Test
        @DisplayName("기존 [A,B] -> 신규 [B,C]: A 삭제, C 추가, B 유지")
        void diff_removeAndAdd() {
            //given
            PostCategory postCategoryA = postCategory(categoryA);
            PostCategory postCategoryB = postCategory(categoryB);

            given(postCategoryRepository.findAllByPost(post)).willReturn(List.of(postCategoryA, postCategoryB));

            //when
            postCategoryService.updatePostCategories(post, List.of(categoryB, categoryC));

            //then: 삭제는 A만
            ArgumentCaptor<List<PostCategory>> removeCaptor = ArgumentCaptor.forClass(List.class);
            verify(postCategoryRepository).deleteAll(removeCaptor.capture());
            assertThat(removeCaptor.getValue())
                    .extracting(postCategory -> postCategory.getCategory().getId())
                    .containsExactly(10L);

            //then: 추가는 C만
            ArgumentCaptor<List<PostCategory>> addCaptor = ArgumentCaptor.forClass(List.class);
            verify(postCategoryRepository).saveAll(addCaptor.capture());
            assertThat(addCaptor.getValue())
                    .extracting(postCategory -> postCategory.getCategory().getId())
                    .containsExactly(30L);
        }

        @Test
        @DisplayName("기존 [A,B] -> 신규 [A,B]: 변경 없음")
        void diff_noChange() {
            //given
            given(postCategoryRepository.findAllByPost(post)).willReturn(List.of(postCategory(categoryA), postCategory(categoryB)));

            //when
            postCategoryService.updatePostCategories(post, List.of(categoryA, categoryB));

            //then
            verify(postCategoryRepository, never()).deleteAll(anyList());
            verify(postCategoryRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("기존 [A] -> 신규 [B,C]: A 삭제, B 추가, C 추가")
        void diff_replaceAll() {
            //given
            PostCategory postCategoryA = postCategory(categoryA);

            given(postCategoryRepository.findAllByPost(post)).willReturn(List.of(postCategoryA));

            //when
            postCategoryService.updatePostCategories(post, List.of(categoryB, categoryC));

            //then: A 삭제
            ArgumentCaptor<List<PostCategory>> removeCaptor = ArgumentCaptor.forClass(List.class);
            verify(postCategoryRepository).deleteAll(removeCaptor.capture());
            assertThat(removeCaptor.getValue())
                    .extracting(postCategory -> postCategory.getCategory().getId())
                    .containsExactly(10L);

            //then: B,C 추가
            ArgumentCaptor<List<PostCategory>> addCaptor = ArgumentCaptor.forClass(List.class);
            verify(postCategoryRepository).saveAll(removeCaptor.capture());
            assertThat(removeCaptor.getValue())
                    .extracting(postCategory -> postCategory.getCategory().getId())
                    .containsExactly(20L, 30L);
        }
    }

    //테스트용 헬퍼
    private Category category(Long id, String type) {
        Category category = Category.builder().build();
        ReflectionTestUtils.setField(category, "id", id);

        return category;
    }

    private PostCategory postCategory(Category category) {
        return PostCategory.builder()
                .category(category)
                .post(post)
                .build();
    }
}