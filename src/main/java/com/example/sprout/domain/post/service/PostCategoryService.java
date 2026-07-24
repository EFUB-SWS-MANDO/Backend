package com.example.sprout.domain.post.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostCategory;
import com.example.sprout.domain.post.repository.PostCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;

    @Transactional
    public void assignPostCategories(Post post, List<Category> categories) {
        List<PostCategory> newPostCategories = categories.stream()
                .map(category -> new PostCategory(category, post))
                .toList();
        postCategoryRepository.saveAll(newPostCategories);
    }

    @Transactional
    public void updatePostCategories(Post post, List<Category> categories) {
        List<PostCategory> existing = postCategoryRepository.findAllByPost(post);

        Set<Long> existingIds = existing.stream()
                .map(postCategory -> postCategory.getCategory().getId())
                .collect(Collectors.toSet());
        Set<Long> newIds = categories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        //삭제
        List<PostCategory> toRemove = existing.stream()
                .filter(postCategory -> !newIds.contains(postCategory.getCategory().getId()))
                .toList();

        //추가
        List<PostCategory> toAdd = categories.stream()
                .filter(category -> !existingIds.contains(category.getId()))
                .map(category -> new PostCategory(category, post))
                .toList();

        if (!toRemove.isEmpty()) postCategoryRepository.deleteAll(toRemove);
        if (!toAdd.isEmpty()) postCategoryRepository.saveAll(toAdd);
    }

    @Transactional
    public void deleteByPost(Post post) {
        postCategoryRepository.deleteAllByPost(post);
    }
}
