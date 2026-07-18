package com.example.sprout.domain.category.initializer;

import com.example.sprout.domain.category.initializer.CategoryDataInitializer;
import com.example.sprout.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class CategoryDataInitializerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryDataInitializer categoryInitializer;

    @Test
    @DisplayName("DB에 카테고리 데이터가 0개면 초기 데이터 저장")
    void run_WhenDBIsEmpty_SavesInitialCategories() throws Exception {
        // given
        given(categoryRepository.count()).willReturn(0L);

        // when
        categoryInitializer.run();

        // then
        verify(categoryRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("DB에 카테고리 데이터가 존재하면 초기 데이터 저장 없이 스킵")
    void run_whenDBIsNotEmpty_SkipsSaving() throws Exception {
        // given
        given(categoryRepository.count()).willReturn(1L);

        // when
        categoryInitializer.run();

        // then
        verify(categoryRepository, never()).saveAll(anyList());
    }

}
