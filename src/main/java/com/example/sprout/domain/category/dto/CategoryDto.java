package com.example.sprout.domain.category.dto;

import java.util.List;

public record CategoryDto(
        List<String> categories
) {
    public static CategoryDto of(List<String> categories) {
        return new CategoryDto(categories);
    }
}
