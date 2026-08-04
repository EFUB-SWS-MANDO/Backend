package com.example.sprout.domain.follow.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.follow.dto.response.FollowCreateResponse;
import com.example.sprout.domain.follow.dto.response.FollowMemberListResponse;
import com.example.sprout.domain.follow.service.FollowService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/{memberId}")
public class FollowController {

    private final FollowService followService;

    // 팔로우 생성
    @PostMapping("/follow")
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
    @DeleteMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> deleteFollow(
            @AuthMember Long requesterId,
            @PathVariable(name = "memberId") Long followeeId
    ) {
        log.info("Follow 취소 요청 - followerId={}, followeeId={}", requesterId, followeeId);

        followService.deleteFollow(requesterId, followeeId);

        return ResponseEntity.ok(ApiResponse.success("팔로우 취소 성공"));
    }

    // 팔로워 목록 조회
    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<FollowMemberListResponse>> getFollowers(
            @AuthMember Long requesterId,
            @PathVariable(name = "memberId") Long targetId,
            @RequestParam(name = "idAfter", required = false) @Positive Long idAfter,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        log.info("Followers 목록 조회 요청 - requesterId={}, targetId={}", requesterId, targetId);

        FollowMemberListResponse response = followService.getFollowers(
                requesterId, targetId, idAfter, limit
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "팔로워 목록 조회 성공",
                        response
                ));
    }

    // 팔로잉 목록 조회
    @GetMapping("/followings")
    public ResponseEntity<ApiResponse<FollowMemberListResponse>> getFollowings(
            @AuthMember Long requesterId,
            @PathVariable(name = "memberId") Long targetId,
            @RequestParam(name = "idAfter", required = false) @Positive Long idAfter,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) @Max(100) int limit
    ) {
        log.info("Followings 목록 조회 요청 - requesterId={}, targetId={}", requesterId, targetId);

        FollowMemberListResponse response = followService.getFollowings(
                requesterId, targetId, idAfter, limit
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "팔로잉 목록 조회 성공",
                        response
                ));
    }

}
