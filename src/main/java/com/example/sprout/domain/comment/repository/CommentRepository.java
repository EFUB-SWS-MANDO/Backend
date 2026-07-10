package com.example.sprout.domain.comment.repository;

import com.example.sprout.domain.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND (:idAfter IS NULL OR c.id > :idAfter)" +
            "ORDER BY c.id ASC")
    List<Comment> findCommentsByPostId(
            @Param("postId") Long postId,
            @Param("idAfter") Long idAfter,
            Pageable pageable
    );

    @Query("SELECT c.parent.id FROM Comment c WHERE c.parent.id IN :parentIds")
    List<Long> findParentIdWithChildren(@Param("parentIds") List<Long> parentIds);

    Long countByPostId(Long postId);
}
