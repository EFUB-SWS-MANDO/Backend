package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    void deleteAllByMember(Member member);
    void deleteAllByPost(Post post);
    @Query("SELECT pl.post.id " +
            "FROM PostLike pl " +
            "WHERE pl.member.id = :requesterId AND pl.post.id IN :postIds")
    List<Long> findLikedPostIdsByMemberIdAndPostIdIn(Long requesterId, List<Long> postIds);
}
