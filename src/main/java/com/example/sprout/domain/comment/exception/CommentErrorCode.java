package com.example.sprout.domain.comment.exception;

import com.example.sprout.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    PARENT_NOT_IN_POST(HttpStatus.BAD_REQUEST, "부모 댓글이 해당 게시글에 존재하지 않습니다."),
    CANNOT_REPLY_TO_DELETED_COMMENT(HttpStatus.BAD_REQUEST, "삭제된 댓글에는 대댓글을 달 수 없습니다."),
    CANNOT_REPLY_TO_REPLY(HttpStatus.BAD_REQUEST,"대댓글에는 댓글을 달 수 없습니다."),
    ALREADY_DELETED_COMMENT(HttpStatus.BAD_REQUEST, "이미 삭제된 댓글은 수정할 수 없습니다."),
    CANNOT_MAKE_REPLY_PUBLIC_WHEN_PARENT_PRIVATE(HttpStatus.BAD_REQUEST, "부모 댓글이 비공개일 경우 대댓글을 공개 처리할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
