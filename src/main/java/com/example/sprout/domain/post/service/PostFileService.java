package com.example.sprout.domain.post.service;

import com.example.sprout.domain.file.dto.response.MovedFileInfo;
import com.example.sprout.domain.file.service.S3FileService;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostFile;
import com.example.sprout.domain.post.repository.PostFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostFileService {

    private final PostFileRepository postFileRepository;

    private final S3FileService s3FileService;

    public void createPostFile(Post post, List<MovedFileInfo> fileInfos) {
        if (fileInfos == null || fileInfos.isEmpty()) return;

        List<PostFile> postFiles = fileInfos.stream()
                .map(fileInfo -> PostFile.builder()
                        .post(post)
                        .s3Key(fileInfo.s3Key())
                        .originalFileName(fileInfo.originalFilename())
                        .contentType(fileInfo.contentType())
                        .build())
                .toList();
        postFileRepository.saveAll(postFiles);
    }

    public void updatePostFile(Post post, List<String> fileKeys) {
        if (fileKeys == null) return;

        List<String> oldFileKeys = getPostFiles(post).stream()
                .map(PostFile::getS3Key).toList();

        List<String> toRemove = oldFileKeys.stream()
                .filter(oldFileKey -> !fileKeys.contains(oldFileKey))
                .toList();
        List<String> toAdd = fileKeys.stream()
                .filter(newFileKey -> !oldFileKeys.contains(newFileKey))
                .toList();
        List<MovedFileInfo> newFileInfos = s3FileService.moveToPermanent(toAdd, post.getId());

        //추가된 파일 생성
        createPostFile(post, newFileInfos);

        //삭제된 파일 삭제
        long deleted = postFileRepository.deleteAllByS3KeyIn(toRemove);
        if (deleted != toRemove.size()) {
            log.warn("파일 삭제 개수 불일치 - 예상: {}, 실제: {}, postId: {}", toRemove.size(), deleted, post.getId());
        }
        if(!toRemove.isEmpty()) s3FileService.deleteFiles(toRemove); //S3에서도 삭제
    }

    public List<PostFile> getPostFiles(Post post) {
        return postFileRepository.findAllByPost(post);
    }

    public void deleteByPost(Post post) {
        List<String> s3Keys = getPostFiles(post).stream()
                .map(PostFile::getS3Key)
                .toList();
        postFileRepository.deleteAllByPost(post);
        if(!s3Keys.isEmpty()) s3FileService.deleteFiles(s3Keys);
    }
}
