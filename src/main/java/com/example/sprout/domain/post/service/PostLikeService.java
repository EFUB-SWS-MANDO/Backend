package com.example.sprout.domain.post.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public void deleteByMember(Member member) {
        postLikeRepository.deleteAllByMember(member);
    }

    @Transactional
    public void deleteByPost(Post post) {
        postLikeRepository.deleteAllByPost(post);
    }
}
