package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostCategory;
import com.example.sprout.domain.post.repository.projection.PostCategoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    List<PostCategory> findAllByPost(Post post);
    void deleteAllByPost(Post post);
    @Query("SELECT pc.post.id AS postId, pc.category.type AS type " +
            "FROM PostCategory  pc " +
            "WHERE pc.post.id IN :postIds")
    List<PostCategoryView> findCategoryTypesByPostIdIn(@Param("postIds") List<Long> postIds);
}
