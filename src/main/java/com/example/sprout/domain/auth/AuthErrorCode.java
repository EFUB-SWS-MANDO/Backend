package com.example.sprout.domain.auth;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_KAKAO_TOKEN(HttpStatus.BAD_REQUEST, "로그인 실패"),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "로그인 실패"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");

    private final HttpStatus status;
    private final String message;
}
