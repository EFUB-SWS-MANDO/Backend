package com.example.sprout.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    //로그인 에러
    INVALID_KAKAO_TOKEN(HttpStatus.BAD_REQUEST, "로그인 실패"),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "로그인 실패"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
