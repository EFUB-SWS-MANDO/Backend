package com.example.sprout.domain.comment.repository;

import com.example.sprout.domain.comment.entity.Comment;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List <Comment> findAllByAuthorId(Long memberId);

    List<Comment> findAllByPost(Post post);

    List<Comment> findAllByAuthor(Member member);
}
