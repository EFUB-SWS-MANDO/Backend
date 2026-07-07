package com.example.sprout.domain.comment.repository;

import com.example.sprout.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>{


}
