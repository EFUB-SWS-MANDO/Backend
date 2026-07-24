package com.example.sprout.domain.post.entity;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "posts",
        indexes = {
            @Index(name="idx_post_author_id", columnList = "author_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, updatable = false)
    private Member author;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "isPrivate")
    private boolean isPrivate;

    @Builder
    public Post(Member author, String title, String content, boolean isPrivate) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.likeCount = 0;
        this.isPrivate = isPrivate;
    }

    public void updatePost(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void addLikeCount() {this.likeCount += 1;}
    public void removeLikeCount() {this.likeCount -= 1;}
}
