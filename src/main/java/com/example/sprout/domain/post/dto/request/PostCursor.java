package com.example.sprout.domain.post.dto.request;


import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.global.error.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public record PostCursor(
        Long id,
        String sortValue
) {
    public static PostCursor of (Post post, String sortBy) {
        String value = "likeCount".equals(sortBy) ?  String.valueOf(post.getLikeCount()) : post.getCreatedAt().toString();
        return new PostCursor(post.getId(), value);
    }

    public String encode() {
        return Base64.getUrlEncoder()
                .encodeToString((id + "|" + sortValue).getBytes(StandardCharsets.UTF_8));
    }

    public static PostCursor decode(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            String[] elements = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8)
                    .split("\\|",2);

            return new PostCursor(Long.valueOf(elements[0]), elements[1]);
        } catch (Exception e) {
            log.warn("게시글 목록 조회 URL 파싱 에러");
            throw new BusinessException(PostErrorCode.INVALID_CURSOR);
        }
    }
}
