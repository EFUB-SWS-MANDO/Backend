package com.example.sprout.domain.profile.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public void deleteByMember(Member member) {
        profileRepository.deleteByMember(member);
    }
}
