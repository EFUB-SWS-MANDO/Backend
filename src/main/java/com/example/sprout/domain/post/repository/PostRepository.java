package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByPostId(Long postId);
}
