package com.example.sprout.domain.follow.exception;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FollowErrorCode implements ErrorCode {

    FOLLOW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 팔로우한 유저입니다."),
    CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "자기 자신은 팔로우 할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
