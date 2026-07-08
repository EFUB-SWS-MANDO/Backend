package com.example.sprout.domain.auth.controller;

import com.example.sprout.domain.auth.dto.request.CreateMemberRequest;
import com.example.sprout.domain.auth.dto.request.ReissueTokenRequest;
import com.example.sprout.domain.auth.dto.response.CreateMemberResponse;
import com.example.sprout.domain.auth.dto.response.ReissueTokenResponse;
import com.example.sprout.domain.auth.service.AuthService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<CreateMemberResponse>> signIn(@RequestBody CreateMemberRequest request) {

        log.info("로그인 요청 - provider: {}", request.provider());
        CreateMemberResponse response = authService.signIn(request);

        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<ReissueTokenResponse>> reissueToken(@RequestBody ReissueTokenRequest request) {
        log.info("토큰 재발급 요청");
        ReissueTokenResponse response = authService.reissueToken(request);

        return ResponseEntity.ok(ApiResponse.success("토큰 재발급 성공", response));
    }

}
