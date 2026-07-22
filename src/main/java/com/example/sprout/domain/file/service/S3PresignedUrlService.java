package com.example.sprout.domain.file.service;

import com.example.sprout.domain.file.dto.request.PresignedUrlRequest;
import com.example.sprout.domain.file.dto.response.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public PresignedUrlResponse createPresignedUrl(Long requesterId, PresignedUrlRequest request) {
        String fileKey = "posts/temp/" + requesterId + "_" + UUID.randomUUID() + "_" + request.fileName();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(request.contentType())
                .build();

        PutObjectPresignRequest presignedRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignedRequest);

        return PresignedUrlResponse.of(presigned.url().toString(), fileKey);
    }
}
