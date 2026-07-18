package com.example.sprout.domain.category.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.category.dto.CategoryDto;
import com.example.sprout.domain.category.service.CategoryService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/categories")
public class CategoryController {

    private  final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<CategoryDto>> getCategories(
            @AuthMember Long memberId
    ) {
        log.info("category 조회 요청, requesterId={}", memberId);

        CategoryDto response = categoryService.getCategories();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "카테고리 조회 성공",
                        response
                )
        );
    }

}
