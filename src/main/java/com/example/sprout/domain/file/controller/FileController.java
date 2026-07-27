package com.example.sprout.domain.file.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.file.dto.request.PresignedUrlRequest;
import com.example.sprout.domain.file.dto.response.PresignedUrlResponse;
import com.example.sprout.domain.file.service.S3PresignedUrlService;
import com.example.sprout.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final S3PresignedUrlService s3PresignedUrlService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(@AuthMember Long requesterId,
                                                                             @Valid @RequestBody PresignedUrlRequest request) {

        PresignedUrlResponse response = s3PresignedUrlService.createPresignedUrl(requesterId, request);

        return ResponseEntity.ok().body(ApiResponse.success("Presigned url 발급 성공", response));
    }
}
