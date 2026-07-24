package com.example.sprout.domain.post.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.post.dto.request.CreatePostRequest;
import com.example.sprout.domain.post.dto.request.PostSearchCondition;
import com.example.sprout.domain.post.dto.request.UpdatePostRequest;
import com.example.sprout.domain.post.dto.response.PostDetailResponse;
import com.example.sprout.domain.post.dto.response.PostListResponse;
import com.example.sprout.domain.post.service.PostService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Slf4j
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostDetailResponse>> createPost(@AuthMember Long authorId,
                                                                      @Valid @RequestBody CreatePostRequest request) {
        log.info("게시글 생성 요청: memberId: {}", authorId);
        PostDetailResponse response = postService.createPost(authorId, request);

        return ResponseEntity.ok(ApiResponse.success("게시글 생성 성공", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPostList(@AuthMember Long memberId,
                                                                     @RequestParam(defaultValue = "createdAt", name = "sortBy") String sortBy,
                                                                     @RequestParam(defaultValue = "Desc",name = "sortDirection") String sortDirection,
                                                                     @RequestParam(required = false, name = "category") List<String> category,
                                                                     @RequestParam(required = false, name = "author") Long author,
                                                                     @RequestParam(defaultValue = "false", name = "followingOnly") boolean followingOnly,
                                                                     @RequestParam(required = false, name = "keyword") String keyword,
                                                                     @RequestParam(required = false, name = "nextCursor") String nextCursor,
                                                                     @RequestParam(defaultValue = "10", name = "limit") int limit
                                                                     ) {

        log.info ("게시글 목록 조회 요청: memberId: {}", memberId);
        PostSearchCondition condition = PostSearchCondition.of(sortBy, sortDirection, category, author, followingOnly, keyword, nextCursor, limit);
        PostListResponse response = postService.getPostList(memberId, condition);

        return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공", response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(@AuthMember Long requesterId,
                                                                         @PathVariable Long postId) {

        log.info("게시글 상세조회 요청: requesterId: {}, postId: {}", requesterId, postId);
        PostDetailResponse response = postService.getPostDetail(requesterId, postId);

        return ResponseEntity.ok(ApiResponse.success("게시글 생성 성공", response));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> updatePost(@AuthMember Long requesterId,
                                                                      @PathVariable Long postId,
                                                                      @Valid @RequestBody UpdatePostRequest request) {

        log.info("게시글 수정 요청: requesterId: {}, postId: {}", requesterId, postId);
        PostDetailResponse response = postService.updatePost(requesterId, postId, request);

        return ResponseEntity.ok(ApiResponse.success("게시글 수정 성공", response));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@AuthMember Long requesterId,
                                                        @PathVariable Long postId) {

        log.info("게시글 삭제 요청: requesterId: {}, postId: {}", requesterId, postId);
        postService.deletePost(requesterId, postId);

        return ResponseEntity.ok(ApiResponse.success("게시글 삭제 성공", null));
    }
}
