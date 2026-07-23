package com.example.sprout.domain.profile.repository;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByMember(Member member);
    boolean existsByMember(Member member);
    List<Profile> findByMemberIn(List<Member> members);
    void deleteByMember(Member member);

    @Query("SELECT p FROM Profile p WHERE p.member.id IN :memberIds")
    List<Profile> findByMemberIdIn(@Param("memberIds") List<Long> memberIds);


}
