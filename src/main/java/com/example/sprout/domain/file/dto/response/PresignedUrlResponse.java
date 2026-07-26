package com.example.sprout.domain.file.dto.response;

public record PresignedUrlResponse(
        String uploadUrl,
        String fileKey
) {
    public static PresignedUrlResponse of(String uploadUrl, String fileKey) {
        return new PresignedUrlResponse(uploadUrl, fileKey);
    }
}
