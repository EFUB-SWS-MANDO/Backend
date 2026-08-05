package com.example.sprout.domain.category.service;

import com.example.sprout.domain.category.dto.CategoryDto;
import com.example.sprout.domain.category.dto.CategoryListResponse;
import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.repository.CategoryRepository;
import com.example.sprout.global.ai.client.AiChatClient;
import com.example.sprout.global.ai.dto.AiChatResponse;
import com.example.sprout.global.ai.exception.AiCallException;
import com.example.sprout.global.ai.prompt.PromptTemplateLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class CategoryClassificationServiceTest {

    @Mock
    private CategoryService categoryService;
    @Mock
    private PromptTemplateLoader promptTemplateLoader;
    @Mock
    private AiChatClient aiChatClient;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryClassificationService classificationService;

    private static final List<String> ALLOWED = List.of("COLLABORATION",  "PROBLEM_SOLVING", "GROWTH", "LEADERSHIP",
            "CREATIVITY", "ETC");

    @BeforeEach
    void setUp() {
        given(promptTemplateLoader.load(anyString()))
                .willReturn(new PromptTemplateLoader.PromptTemplate("dummy prompt"));

        List<CategoryDto> categoryDtos = new ArrayList<>();
        for (int i = 0; i < ALLOWED.size(); i++) {
            Long idIdx = (long) i + 1;
            categoryDtos.add(new CategoryDto(idIdx, ALLOWED.get(i)));
        }

        given(categoryService.getCategories()).willReturn(CategoryListResponse.of(categoryDtos));
    }

    private Category category(String type) {
        return Category.builder().type(type).build();
    }

    @Test
    @DisplayName("AI 카테고리 할당 성공")
    void classify_Success() {
        //given
        given(aiChatClient.chat(any())).willReturn(new AiChatResponse("COLLABORATION, GROWTH"));
        given(categoryRepository.findAllByTypeIn(List.of("COLLABORATION", "GROWTH")))
                .willReturn(List.of(category("COLLABORATION"), category("GROWTH")));

        //when
        List<Category> result = classificationService.classifyCategory("제목", "협업하며 성장한 경험");

        //then
        assertThat(result).extracting(Category::getType).containsExactly("COLLABORATION", "GROWTH");

        verify(categoryRepository).findAllByTypeIn(List.of("COLLABORATION", "GROWTH"));
    }

    @Test
    @DisplayName("AI 카테고리 할당 성공: 목록에 없는 카테고리 할당 필터링")
    void classify_FiltersInvalid() {
        //given
        given(aiChatClient.chat(any())).willReturn(new AiChatResponse("COLLABORATION, 음식, GROWTH"));
        given(categoryRepository.findAllByTypeIn(List.of("COLLABORATION", "GROWTH")))
                .willReturn(List.of(category("COLLABORATION"), category("GROWTH")));

        //when
        classificationService.classifyCategory("제목", "내용");

        //then
        verify(categoryRepository).findAllByTypeIn(List.of("COLLABORATION", "GROWTH"));
    }

    @Test
    @DisplayName("AI 카테고리 할당 성공: 유효 카테고리 3개로 제한")
    void classify_LimitsToMax() {
        //given
        given(aiChatClient.chat(any())).willReturn(new AiChatResponse("COLLABORATION, PROBLEM_SOLVING, GROWTH, LEADERSHIP, CREATIVITY"));
        given(categoryRepository.findAllByTypeIn(List.of("COLLABORATION", "PROBLEM_SOLVING", "GROWTH")))
                .willReturn(List.of(category("COLLABORATION"), category("PROBLEM_SOLVING"), category("GROWTH")));

        //when
        List<Category> result = classificationService.classifyCategory("제목", "내용");

        //then
        assertThat(result).hasSize(3);
        verify(categoryRepository).findAllByTypeIn(List.of("COLLABORATION", "PROBLEM_SOLVING", "GROWTH"));
    }

    @Test
    @DisplayName("AI 카테고리 할당 실패: 유효 카테고리 할당 실패 - 실패 시 ETC로 fallback")
    void classify_EmptyValid_FallbackEtc() {
        //given
        given(aiChatClient.chat(any())).willReturn(new AiChatResponse("음식, 게임"));
        given(categoryRepository.findAllByTypeIn(List.of("ETC")))
                .willReturn(List.of(category("ETC")));

        //when
        List<Category> result = classificationService.classifyCategory("제목", "내용");

        //then
        assertThat(result).extracting(Category::getType).containsExactly("ETC");
        verify(categoryRepository).findAllByTypeIn(List.of("ETC"));
    }

    @Test
    @DisplayName("AI 카테고리 할당 실패: AI 호출 실패 - 실패 시 ETC로 fallback")
    void classify_AiThrows_FallbackEtc() {
        //given
        given(aiChatClient.chat(any())).willThrow(new AiCallException("AI 호출 실패"));
        given(categoryRepository.findAllByTypeIn(List.of("ETC")))
                .willReturn(List.of(category("ETC")));

        //when
        List<Category> result = classificationService.classifyCategory("제목", "내용");

        //then
        assertThat(result).extracting(Category::getType).containsExactly("ETC");
        verify(categoryRepository).findAllByTypeIn(List.of("ETC"));
    }
}