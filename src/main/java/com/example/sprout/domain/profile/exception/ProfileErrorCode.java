package com.example.sprout.domain.profile.exception;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {

    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 프로필입니다."),
    PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 프로필이 존재합니다.");

    private final HttpStatus status;
    private final String message;
}
