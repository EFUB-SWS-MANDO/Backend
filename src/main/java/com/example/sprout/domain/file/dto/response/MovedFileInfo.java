package com.example.sprout.domain.file.dto.response;

public record MovedFileInfo(
        String s3Key,
        String originalFilename,
        String contentType
) {
    public static MovedFileInfo of(String s3Key, String originalFilename, String contentType) {
        return new MovedFileInfo(s3Key, originalFilename, contentType);
    }
}
