package com.example.sprout.domain.post.exception;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostLikeErrorCode implements ErrorCode {

    POST_LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 좋아요를 누른 글입니다."),
    POST_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "좋아요를 누르지 않은 글입니다.");

    private final HttpStatus status;
    private final String message;
}
