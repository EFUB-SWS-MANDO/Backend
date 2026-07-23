package com.example.sprout.domain.post.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.post.dto.response.PostLikeResponse;
import com.example.sprout.domain.post.service.PostLikeService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/likes")
@Slf4j
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostLikeResponse>> createPostLike(@AuthMember Long memberId,
                                                                        @PathVariable Long postId) {

        log.info("게시글 좋아요 생성 요청 - memberId: {}, postId: {}", memberId, postId);
        PostLikeResponse response = postLikeService.createPostLike(memberId, postId);

        return ResponseEntity.ok(ApiResponse.success("게시글 좋아요 생성 성공", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<PostLikeResponse>> deletePostLike(@AuthMember Long memberId,
                                                                        @PathVariable Long postId) {

        log.info("게시글 좋아요 삭제 요청 - memberId: {}, postId: {}", memberId, postId);
        PostLikeResponse response = postLikeService.deletePostLike(memberId, postId);

        return ResponseEntity.ok(ApiResponse.success("게시글 좋아요 삭제 성공", response));
    }
}
