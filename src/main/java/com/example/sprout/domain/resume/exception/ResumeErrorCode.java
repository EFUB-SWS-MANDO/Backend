package com.example.sprout.domain.resume.exception;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResumeErrorCode implements ErrorCode{

    AI_ANSWER_MISSING(HttpStatus.FAILED_DEPENDENCY, "AI 응답이 누락되었습니다."),
    AI_RESPONSE_PARSE_FAILED(HttpStatus.BAD_GATEWAY, "AI 응답 JSON 파싱에 실패했습니다."),
    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 자기소개서입니다."),
    RESUME_ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}