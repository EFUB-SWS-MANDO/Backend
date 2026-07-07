package com.example.sprout.domain.member.repository;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthIdAndOauthProvider(String oauthId, OauthProvider oauthProvider);
}
