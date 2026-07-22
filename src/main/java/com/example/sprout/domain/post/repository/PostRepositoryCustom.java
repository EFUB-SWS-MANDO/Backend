package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.post.dto.request.PostCursor;
import com.example.sprout.domain.post.dto.request.PostSearchCondition;
import com.example.sprout.domain.post.entity.Post;

import java.util.List;


public interface PostRepositoryCustom {
    List<Post> search(PostSearchCondition condition, Long requesterId, PostCursor cursor);

    long count(PostSearchCondition condition, Long requesterId);
}
