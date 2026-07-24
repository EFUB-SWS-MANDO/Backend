package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {

    void deleteAllByPost(Post post);

    @Query("SELECT pc FROM PostCategory pc " +
            "JOIN FETCH pc.post " +
            "JOIN FETCH pc.category " +
            "WHERE pc.post.id IN :postIds")
    List<PostCategory> findAllByPostIdIn(@Param("postIds") List<Long> postIds);
}
