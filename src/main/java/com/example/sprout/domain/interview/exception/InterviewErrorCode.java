package com.example.sprout.domain.interview.exception;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InterviewErrorCode implements ErrorCode {

    INTERVIEW_NOT_COMPLETED(HttpStatus.CONFLICT, "면접이 종료되지 않았습니다."),
    INTERVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 모의면접이 존재하지 않습니다."),
    INTERVIEW_FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 모의면접에 대한 총평이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;

}
