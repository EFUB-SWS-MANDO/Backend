package com.example.sprout.domain.category.service;

import com.example.sprout.domain.category.dto.CategoryDto;
import com.example.sprout.domain.category.dto.CategoryListResponse;
import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.repository.CategoryRepository;
import com.example.sprout.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

public class CategoryServiceTest extends IntegrationTestSupport {

    @Autowired
    private CategoryService categoryService;

    @MockitoSpyBean
    private CategoryRepository categoryRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        categoryRepository.saveAll(
                List.of(
                        Category.builder().type("COLLABORATION").build()
                )
        );

        clearInvocations(categoryRepository);
    }


    @Test
    @DisplayName("첫 조회 - DB 조회, 두 번째 조회 - 캐시 조회")
    void getCategories() {
        // when
        CategoryListResponse first = categoryService.getCategories();

        await()
                .atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(20))
                .untilAsserted(() -> {
                    Boolean hasKey = redisTemplate.hasKey("categories::list");
                    assertThat(hasKey).isTrue();
                });

        CategoryListResponse second = categoryService.getCategories();

        // then
        assertThat(first.categories()).extracting(CategoryDto::type).containsExactly("COLLABORATION");
        assertThat(second).isEqualTo(first);
        verify(categoryRepository, times(1)).findAll();
    }
}
