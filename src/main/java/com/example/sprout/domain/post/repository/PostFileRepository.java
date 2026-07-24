package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostFileRepository extends JpaRepository<PostFile, Long> {
    List<PostFile> findAllByPost(Post post);

    long deleteAllByS3KeyIn(List<String> toRemove);

    void deleteAllByPost(Post post);
}
