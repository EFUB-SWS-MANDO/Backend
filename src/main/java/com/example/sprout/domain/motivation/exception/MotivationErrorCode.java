package com.example.sprout.domain.motivation.exception;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MotivationErrorCode implements ErrorCode {

    MOTIVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "동기부여 문구가 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
