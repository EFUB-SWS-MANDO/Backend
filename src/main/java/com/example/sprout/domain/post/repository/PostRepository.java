package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.dto.request.PostSearchCondition;
import com.example.sprout.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    List<Post> findAllByAuthor (Member author);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Post p 
            SET p.likeCount = p.likeCount + 1 
            WHERE p.id = :postId
            """)
    void incrementLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Post p
            SET p.likeCount = CASE WHEN p.likeCount > 0
            THEN p.likeCount - 1
            ELSE 0 END
            WHERE p.id = :postId
        """)
    void decrementLikeCount(@Param("postId") Long postId);

    @Query("SELECT p.likeCount FROM Post p WHERE p.id = :postId")
    int findLikeCountById(@Param("postId") Long postId);

    int countAllByAuthor(Member member);

    List<Post> findTop4ByAuthorOrderByUpdatedAtDesc(Member member);

    @Query("SELECT COALESCE(SUM(p.likeCount), 0) FROM Post p WHERE p.author = :author")
    long sumLikeCountByAuthor(@Param("author") Member member);
}
