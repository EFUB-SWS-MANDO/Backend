package com.example.sprout.domain.post.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.post.dto.request.CreatePostRequest;
import com.example.sprout.domain.post.dto.response.PostDetailDto;
import com.example.sprout.domain.post.service.PostService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Slf4j
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailDto>> createPost(@AuthMember Long authorId,
                                                                 @ Valid @RequestBody CreatePostRequest request) {
        log.info("게시글 생성 요청: memberId: {}", authorId);
        PostDetailDto response = postService.createPost(authorId, request);

        return ResponseEntity.ok(ApiResponse.success("게시글 생성 성공", response));
    }
}
