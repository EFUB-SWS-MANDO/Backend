package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {

    void deleteAllByPost(Post post);
}
