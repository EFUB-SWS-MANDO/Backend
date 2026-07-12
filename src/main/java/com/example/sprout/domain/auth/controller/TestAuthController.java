package com.example.sprout.domain.auth.controller;

import com.example.sprout.domain.auth.jwt.JwtUtil;
import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Profile("dev")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestAuthController {

    private final JwtUtil jwtUtil;

    //테스트 액세스 토큰 발급 API
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<String>> issueTestToken(@RequestParam("memberId") Long memberId) {
        String testAuthToken = jwtUtil.createAccessToken(memberId);

        return ResponseEntity.ok(ApiResponse.success("테스트 토큰 발급", testAuthToken));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Long>> authTest(@AuthMember Long memberId) {

        return ResponseEntity.ok(ApiResponse.success("토큰 인증 성공", memberId));
    }
}
