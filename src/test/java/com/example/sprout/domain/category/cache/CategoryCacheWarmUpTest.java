package com.example.sprout.domain.category.cache;

import com.example.sprout.domain.category.cache.CategoryCacheWarmUp;
import com.example.sprout.domain.category.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CategoryCacheWarmUpTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryCacheWarmUp categoryCacheWarmUp;

    @Test
    @DisplayName("ApplicationReadyEvent 발생 시 카테고리 목록을 조회하여 캐시 웜업")
    void warmUp() {
        // when
        categoryCacheWarmUp.warmUp();

        // then
        verify(categoryService, times(1)).getCategories();
    }

}
