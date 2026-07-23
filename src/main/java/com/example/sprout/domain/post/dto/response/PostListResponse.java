package com.example.sprout.domain.post.dto.response;

import com.example.sprout.domain.post.entity.Post;
import lombok.Builder;

import java.util.List;

@Builder
public record PostListResponse(
        List<PostSummaryDto> posts,
        String nextCursor,
        boolean hasNext,
        String sortBy,
        String sortDirection,
        Long totalElements
) {
    public static PostListResponse of (List<PostSummaryDto> postSummaryList, String nextCursor, boolean hasNext,
                                       String sortBy, String sortDirection, Long totalElements) {
        return PostListResponse.builder()
                .posts(postSummaryList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .totalElements(totalElements)
                .build();
    }
}
