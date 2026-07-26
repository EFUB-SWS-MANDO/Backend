package com.example.sprout.domain.post.summary;

import com.example.sprout.domain.profile.entity.Profile;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record SummaryLookup(
        Map<Long, Profile> profileMap,
        Set<Long> followingIds,
        Map<Long, Long> commentCountMap,
        Set<Long> likedPostIds,
        Map<Long, List<String>> categoriesMap
) {}
