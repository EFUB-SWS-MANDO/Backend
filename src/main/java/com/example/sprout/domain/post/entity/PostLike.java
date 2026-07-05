package com.example.sprout.domain.post.entity;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.global.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "post_likes",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"post_id", "member_id"})
        },
        indexes = {
            @Index(name = "idx_post_like_post_id", columnList = "post_id"),
            @Index(name = "idx_post_like_member_id", columnList = "member_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Builder
    public PostLike(Post post, Member member) {
        this.post = post;
        this.member = member;
    }
}
