package com.example.sprout.domain.follow.dto.response;

import java.util.List;

public record FollowMemberListResponse(
        List<FollowMemberResponse> members,
        Long nextIdAfter,
        boolean hasNext
) {
    public static FollowMemberListResponse of(
            List<FollowMemberResponse> members, Long nextIdAfter, boolean hasNext
    ) {
        return new FollowMemberListResponse(members, nextIdAfter, hasNext);
    }
}
