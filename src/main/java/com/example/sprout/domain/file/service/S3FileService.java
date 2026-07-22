package com.example.sprout.domain.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 여러 파일 처리
    public List<String> moveToPermanent(List<String> tempFileKeys, Long postId) {
        return tempFileKeys.stream()
                .map(tempFileKey -> moveToPermanent(tempFileKey, postId))
                .toList();
    }


    // Helper 함수

    // temp 경로 파일 -> posts/{postId}/ 경로로 이동 (복사 후 원본 삭제)
    private String moveToPermanent(String tempFileKey, Long postId) {
        String fileName = extractFileName(tempFileKey);
        String permanentKey = "posts/" + postId + "/" + fileName;

        copyObject(tempFileKey, permanentKey);
        deleteObject(tempFileKey);

        return permanentKey;
    }

    private String extractFileName(String key) {
        return key.substring(key.lastIndexOf("/") + 1);
    }

    private void copyObject(String sourceKey, String destinationKey) {
        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(sourceKey)
                .destinationBucket(bucket)
                .destinationKey(destinationKey)
                .build();
        s3Client.copyObject(copyRequest);
    }

    private void deleteObject(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            // temp 파일은 lifecycle Rule로 2일 뒤 자동 정리됨
            log.warn("temp 파일 삭제 실패 (Lifecycle Rule로 자동 정리 예정): key={}", key, e);
        }
    }
}
