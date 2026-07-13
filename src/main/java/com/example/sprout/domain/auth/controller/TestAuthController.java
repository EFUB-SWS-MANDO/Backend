package com.example.sprout.domain.auth.controller;

import com.example.sprout.domain.auth.dto.response.TestTokenResponse;
import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.auth.service.TestAuthService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestAuthController {

    private final TestAuthService testAuthService;

    //테스트 액세스 토큰 발급 API
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TestTokenResponse>> issueTestToken() {
        TestTokenResponse response = testAuthService.getTestTokenAndMember();

        return ResponseEntity.ok(ApiResponse.success("테스트 멤버 생성 및 토큰 발급 성공", response));
    }

    @PostMapping("/member/{memberId}/token")
    public ResponseEntity<ApiResponse<String>> getTestTokenByMember(@PathVariable Long memberId) {
        log.info("테스트 토큰 발급 요청 - memberId: {}", memberId);
        String testAuthToken = testAuthService.getTestTokenByMemberId(memberId);

        return ResponseEntity.ok(ApiResponse.success("테스트 토큰 발급", testAuthToken));
    }

    @PostMapping("/member")
    public ResponseEntity<ApiResponse<Long>> createTestMember() {
        log.info("테스트 멤버 생성 요청");
        Long memberId = testAuthService.createTestMember();

        return ResponseEntity.ok(ApiResponse.success("테스트 멤버 생성 성공", memberId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Long>> authTest(@AuthMember Long memberId) {

        return ResponseEntity.ok(ApiResponse.success("토큰 인증 성공", memberId));
    }
}
