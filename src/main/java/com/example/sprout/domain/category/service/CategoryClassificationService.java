package com.example.sprout.domain.category.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.parser.CategoryResponseParser;
import com.example.sprout.domain.category.repository.CategoryRepository;
import com.example.sprout.global.ai.client.AiChatClient;
import com.example.sprout.global.ai.dto.AiChatRequest;
import com.example.sprout.global.ai.dto.AiChatResponse;
import com.example.sprout.global.ai.dto.AiMessage;
import com.example.sprout.global.ai.exception.AiCallException;
import com.example.sprout.global.ai.prompt.PromptTemplateLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryClassificationService {


    private final CategoryService categoryService;
    private final PromptTemplateLoader promptTemplateLoader;
    private final AiChatClient aiChatClient;

    private static final int MAX_CATEGORIES = 3;
    private final CategoryRepository categoryRepository;

    public List<Category> classifyCategory(String title, String content) {

        List<String> allowed = categoryService.getCategories().categories();

        //프롬프트
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(title, content);

        //OpenAI 호출
        List<String> classified = requestClassification(systemPrompt, userPrompt);
        //카테고리 검증
        List<String> validated = classified.stream()
                .filter(allowed::contains)
                .limit(MAX_CATEGORIES)
                .toList();

        if(validated.isEmpty()) {
            log.warn("AI 분류 유효 카테고리 없음 - classified: {}", classified);
            validated = List.of("ETC");
        }

        return categoryRepository.findAllByTypeIn(validated);
    }

    //system 프롬프트 생성
    private String buildSystemPrompt() {
        return promptTemplateLoader
                .load("category-classification-system.txt")
                .raw();
    }

    private String buildUserPrompt(String title, String content) {
        return promptTemplateLoader
                .load("category-classification-user.txt")
                .render(Map.of(
                        "title", title,
                        "content", content));
    }

    private List<String> requestClassification(String systemPrompt, String userPrompt) {
        //AI 요청
        List<AiMessage> aiMessageList = List.of(
                new AiMessage("system", systemPrompt),
                new AiMessage("user", userPrompt)
        );
        AiChatRequest request = new AiChatRequest(aiMessageList, 0.0, 100);

        try{
            AiChatResponse rawResponse  = aiChatClient.chat(request);
            return CategoryResponseParser.parse(rawResponse.content());
        } catch (Exception e) {
            log.warn("AI 카테고리 분류 실패 - fallback(ETC) 적용", e);
            return List.of();
        }
    }

}
