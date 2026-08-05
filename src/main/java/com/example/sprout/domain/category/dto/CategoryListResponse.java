package com.example.sprout.domain.category.dto;


import java.util.List;

public record CategoryListResponse(
        List<CategoryDto> categories
) {
    public static CategoryListResponse of(List<CategoryDto> categories) {
        return new CategoryListResponse(categories);
    }
}
