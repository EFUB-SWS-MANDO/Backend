package com.example.sprout.domain.post.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.post.dto.request.CreatePostRequest;
import com.example.sprout.domain.post.dto.request.UpdatePostRequest;
import com.example.sprout.domain.post.dto.response.PostDetailDto;
import com.example.sprout.domain.post.service.PostService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Slf4j
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailDto>> createPost(@AuthMember Long authorId,
                                                                 @Valid @RequestBody CreatePostRequest request) {
        log.info("게시글 생성 요청: memberId: {}", authorId);
        PostDetailDto response = postService.createPost(authorId, request);

        return ResponseEntity.ok(ApiResponse.success("게시글 생성 성공", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailDto>> getPostDetail(@AuthMember Long requesterId,
                                                                    @PathVariable Long postId) {

        log.info("게시글 상세조회 요청: requesterId: {}, postId: {}", requesterId, postId);
        PostDetailDto response = postService.getPostDetail(requesterId, postId);

        return ResponseEntity.ok(ApiResponse.success("게시글 생성 성공", response));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailDto>> updatePost(@AuthMember Long requesterId,
                                                                 @PathVariable Long postId,
                                                                 @Valid @RequestBody UpdatePostRequest request) {

        log.info("게시글 수정 요청: requesterId: {}, postId: {}", requesterId, postId);
        PostDetailDto response = postService.updatePost(requesterId, postId, request);

        return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", response));
    }
}
