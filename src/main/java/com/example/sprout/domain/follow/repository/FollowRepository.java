package com.example.sprout.domain.follow.repository;

import com.example.sprout.domain.follow.entity.Follow;
import com.example.sprout.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    // 영향을 받은 행(Row) 개수를 정수로 반환
    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.followee.id = :followeeId")
    int deleteByFollowerIdAndFolloweeId(
            @Param("followerId") Long followerId,
            @Param("followeeId") Long followeeId
    );

    @Query("""
        SELECT f
        FROM Follow f
        WHERE f.followee.id = :memberId
        AND (:idAfter IS NULL OR f.id < :idAfter)
        ORDER BY f.id DESC
    """)
    List<Follow> findFollowersByFolloweeId(Long memberId, Long idAfter, Pageable pageable);

    @Query("""
        SELECT f
        FROM Follow f
        WHERE f.follower.id = :memberId
        AND (:idAfter IS NULL OR f.id < :idAfter)
        ORDER BY f.id DESC
    """)
    List<Follow> findFollowingsByFollowerId(Long memberId, Long idAfter, Pageable pageable);


    @Query("SELECT f.followee.id " +
            "FROM Follow f " +
            "WHERE f.follower.id = :memberId AND f.followee.id IN :followerIds")
    Set<Long> findFollowingIdsAmong(
            @Param("memberId") Long memberId,
            @Param("followerIds") List<Long> followerIds
    );


    int countByFollowee(Member followee);
    int countByFollower(Member follower);

    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower = :member OR f.followee = :member")
    void deleteByFollowerOrFollowee(@Param("member") Member member);

    @Query("SELECT f.followee.id FROM Follow f " +
            "WHERE f.follower.id = :followerId AND f.followee.id IN :followeeIds")
    List<Long> findFolloweeIdsByFollowerIdAndFolloweeIdIn(@Param("followerId")Long followerId, @Param("followeeIds") List<Long> followeeIds);


}
