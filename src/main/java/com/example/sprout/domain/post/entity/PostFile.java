package com.example.sprout.domain.post.entity;

import com.example.sprout.global.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "post_files",
        indexes = {
            @Index(name = "idx_post_file_s3_key", columnList = "s3_key")
        }
)
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
public class PostFile extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "original_filename", nullable = false)
    private String originalFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Builder
    public PostFile (Post post, String s3Key, String originalFileName, String contentType) {
        this.post = post;
        this.s3Key = s3Key;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
    }
}
