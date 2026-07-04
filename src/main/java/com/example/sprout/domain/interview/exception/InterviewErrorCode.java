package com.example.sprout.domain.interview.exception;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InterviewErrorCode implements ErrorCode {

    INTERVIEW_NOT_COMPLETED(HttpStatus.CONFLICT, "면접이 종료되지 않았습니다.");

    private final HttpStatus status;
    private final String message;

}
