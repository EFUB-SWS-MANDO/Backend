package com.example.sprout.domain.auth.controller;

import com.example.sprout.domain.auth.dto.request.SignInRequest;
import com.example.sprout.domain.auth.dto.response.SignInResponse;
import com.example.sprout.domain.auth.service.AuthService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<SignInResponse>> signIn(@Valid @RequestBody SignInRequest request) {

        log.info("로그인 요청 - provider: {}", request.provider());
        SignInResponse response = authService.signIn(request);

        return ResponseEntity.ok(ApiResponse.success("로그인 성공", response));
    }
}
