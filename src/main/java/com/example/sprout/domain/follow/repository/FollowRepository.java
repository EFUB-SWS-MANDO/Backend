package com.example.sprout.domain.follow.repository;

import com.example.sprout.domain.follow.entity.Follow;
import com.example.sprout.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    // 영향을 받은 행(Row) 개수를 정수로 반환
    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower = :followerId AND f.followee = :followeeId")
    int deleteByFollowerIdAndFolloweeId(
            @Param("followerId") Long followerId,
            @Param("followeeId") Long followeeId
    );

    int countByFollowee(Member followee);
    int countByFollower(Member follower);
}
