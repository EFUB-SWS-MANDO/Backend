package com.example.sprout.domain.comment.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.comment.dto.request.CreateCommentRequest;
import com.example.sprout.domain.comment.dto.response.CommentResponse;
import com.example.sprout.domain.comment.service.CommentService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@AuthMember Long requesterId,
                                                                      @PathVariable("postId") Long postId,
                                                                      @Valid @RequestBody CreateCommentRequest request) {
        log.info("댓글 생성 API 호출 - postId: {}", postId);

        CommentResponse response = commentService.createComment(requesterId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("댓글 생성 성공", response));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@AuthMember Long requesterId,
                                                           @PathVariable("commentId") Long commentId) {

        log.info("댓글 삭제 API 요청 - commentId: {}", commentId);

        commentService.deleteComment(requesterId, commentId);

        return ResponseEntity.ok(ApiResponse.success("댓글 삭제 성공"));
    }
}
