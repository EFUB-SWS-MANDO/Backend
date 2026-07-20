package com.example.sprout.domain.post.service;

import com.example.sprout.domain.comment.service.CommentService;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final PostCategoryService postCategoryService;

    @Transactional
    public void deletePostByMember(Member member) {
        List<Post> postList = postRepository.findAllByAuthor(member);
        postList.forEach(this::deletePost);
    }

    //Post 단일 삭제
    private void deletePost(Post post) {
        //Post를 FK로 가지는 자식 엔티티 우선 삭제
        commentService.deleteByPost(post);
        postLikeService.deleteByPost(post);
        postCategoryService.deleteByPost(post);

        //Post 삭제
        postRepository.delete(post);
    }
}
