package com.example.sprout.domain.follow.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.follow.dto.response.FollowCreateResponse;
import com.example.sprout.domain.follow.service.FollowService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/{memberId}/follow")
public class FollowController {

    private final FollowService followService;

    // 팔로우 생성
    @PostMapping
    public ResponseEntity<ApiResponse<FollowCreateResponse>> createFollow(
            @AuthMember Long requesterId,
            @PathVariable(name = "memberId") Long followeeId
    ) {
        log.info("Follow 생성 요청 - followerId={}, followeeId={}", requesterId, followeeId);

        FollowCreateResponse response = followService.createFollow(requesterId, followeeId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("팔로우 생성 성공", response));
    }

    // 팔로우 취소
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFollow(
            @AuthMember Long requesterId,
            @PathVariable(name = "memberId") Long followeeId
    ) {
        log.info("Follow 취소 요청 - followerId={}, followeeId={}", requesterId, followeeId);

        followService.deleteFollow(requesterId, followeeId);

        return ResponseEntity.ok(ApiResponse.success("팔로우 취소 성공"));
    }

}
