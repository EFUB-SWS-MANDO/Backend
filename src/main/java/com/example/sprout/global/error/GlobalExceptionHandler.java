package com.example.sprout.global.error;

import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unexpected exception occurred", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("서버 오류가 발생했습니다."));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        log.error("BusinessException 발생 - errorCode: {}, message: {}",
                exception.getErrorCode(), exception.getMessage());
        return ResponseEntity
                .status(exception.getErrorCode().getStatus())
                .body(ApiResponse.fail(exception.getErrorCode().getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getDefaultMessage()
                : "잘못된 요청입니다.";

        log.error("Validation 실패 - field: {}, reason: {}",
                fieldError != null ? fieldError.getField() : "unknown", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintValidation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("잘못된 요청입니다.");

        log.error("Validation 실패 - reason: {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError != null ?
                String.format("'%s' 요청 값이 올바르지 않습니다.", fieldError.getField()) : "잘못된 요청입니다.";

        log.error("Binding 실패 - field: {}, rejectedValue: {}",
                fieldError != null ? fieldError.getField() : "unknown",
                fieldError != null ? fieldError.getRejectedValue() : "unknown");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception
    ) {

        Throwable cause = exception.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType().isEnum()) {

            String fieldName = invalidFormatException.getPath().stream()
                    .findFirst()
                    .map(reference -> reference.getPropertyName())
                    .orElse("field");

            log.error("Enum 변환 실패 - field: {}, value: {}",
                    fieldName,
                    invalidFormatException.getValue());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(
                            String.format("'%s' 값이 올바르지 않습니다.", fieldName)
                    ));
        }

        log.error("Request Body 파싱 실패", exception);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("요청 형식이 올바르지 않습니다."));
    }

}
