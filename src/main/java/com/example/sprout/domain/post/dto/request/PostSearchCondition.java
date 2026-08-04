package com.example.sprout.domain.post.dto.request;

import jakarta.validation.constraints.Max;
import org.springframework.web.bind.annotation.BindParam;

import java.util.List;

public record PostSearchCondition(
        String sortBy,
        String sortDirection,
        List<String> category,
        Long author,
        Boolean followingOnly,
        String keyword,
        @BindParam("nextCursor")
        String cursor,
        @Max(50)
        Integer limit

) {
    public PostSearchCondition {

        if (sortBy == null || sortBy.isBlank()) sortBy = "createdAt";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "desc";
        if (followingOnly == null) followingOnly = false;
        if (limit == null || limit <= 0) limit = 10;
    }
}
