package com.example.sprout.domain.file.dto.request;

import com.example.sprout.domain.file.enums.UploadType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUrlRequest(
        @NotBlank(message = "파일 명은 필수입니다.")
        String fileName,

        @NotBlank(message = "파일 타입은 필수입니다.")
        String contentType,

        @NotNull
        UploadType uploadType
) {
}
