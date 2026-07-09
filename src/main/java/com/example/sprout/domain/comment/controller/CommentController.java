package com.example.sprout.domain.comment.controller;

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
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@PathVariable("postId") Long postId,
                                                                      @Valid @RequestBody CreateCommentRequest request) {
        log.info("댓글 생성 API 호출 - postId: {}", postId);

        // TODO: 나중에 memberId 추출해서 사용하는 걸로 변경
        Long requesterId = 1L;

        CommentResponse response = commentService.createComment(requesterId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("댓글 생성 성공", response));
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable("commentId") Long commentId) {

        log.info("댓글 삭제 API 요청 - commentId: {}", commentId);

        // TODO 사용자 ID 추후 @AuthMember 사용하여 변경
        Long requesterId = 1L;

        commentService.deleteComment(requesterId, commentId);

        return ResponseEntity.ok(ApiResponse.success("댓글 삭제 성공"));
    }
}
