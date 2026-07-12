package com.example.sprout.domain.category.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories")
    public List<String> getCategories() {
        log.info("[Cache Miss] 카테고리 목록 DB 조회");
        return categoryRepository.findAll()
                .stream()
                .map(Category::getType)
                .toList();
    }

}
