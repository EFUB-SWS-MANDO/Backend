package com.example.sprout.domain.comment.repository;

import com.example.sprout.domain.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
        SELECT c FROM Comment c
        WHERE c.post.id = :postId
          AND c.parent IS NULL
          AND (:idAfter IS NULL OR c.id > :idAfter)
        ORDER BY c.id ASC
    """)
    List<Comment> findParentCommentsByPostId(
            @Param("postId") Long postId,
            @Param("idAfter") Long idAfter,
            Pageable pageable
    );

    // 2) 주어진 부모들에 속한 자식 댓글 전부 (개수 제한 없음)
    @Query("""
        SELECT c FROM Comment c
        WHERE c.threadRootId IN :parentIds
        ORDER BY c.threadRootId ASC, c.id ASC
    """)
    List<Comment> findChildrenByThreadRootIds(@Param("parentIds") List<Long> parentIds);

    Long countByPostIdAndParentIsNull(Long postId);

    List <Comment> findAllByAuthorId(Long memberId);

    List<Comment> findAllByPost(Post post);

    List<Comment> findAllByAuthor(Member member);
}
