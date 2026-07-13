package com.example.sprout.domain.profile.repository;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByMember(Member member);

    List<Profile> findByMemberIn(List<Member> members);
    void deleteByMember(Member member);
}
