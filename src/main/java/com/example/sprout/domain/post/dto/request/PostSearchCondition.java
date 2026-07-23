package com.example.sprout.domain.post.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record PostSearchCondition(
        String sortBy,
        String sortDirection,
        List<String> category,
        Long author,
        boolean followingOnly,
        String keyword,
        String cursor,
        int limit

) {
    public static PostSearchCondition of (String sortBy, String sortDirection,
                                          List<String> category, Long author, boolean followingOnly,
                                          String keyword, String cursor, int limit) {

        return PostSearchCondition.builder()
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .category(category)
                .author(author)
                .followingOnly(followingOnly)
                .keyword(keyword)
                .cursor(cursor)
                .limit(limit)
                .build();
    }
}
