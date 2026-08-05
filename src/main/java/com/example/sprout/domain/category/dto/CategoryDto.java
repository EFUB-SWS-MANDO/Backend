package com.example.sprout.domain.category.dto;

import com.example.sprout.domain.category.entity.Category;


public record CategoryDto(
        Long id,
        String type
) {
    public static CategoryDto of(Category category) {
        return new CategoryDto(category.getId(), category.getType());
    }
}
