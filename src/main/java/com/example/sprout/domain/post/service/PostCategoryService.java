package com.example.sprout.domain.post.service;

import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;

    @Transactional
    public void deleteByPost(Post post) {
        postCategoryRepository.deleteAllByPost(post);
    }
}
