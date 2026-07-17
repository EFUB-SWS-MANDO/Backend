package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.yaml.snakeyaml.events.Event;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByAuthor (Member author);

    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.postCategories " +
            "WHERE p.id IN :ids")
    List<Post> findAllByIdWithCategories(@Param("ids") List<Long> ids);
}
