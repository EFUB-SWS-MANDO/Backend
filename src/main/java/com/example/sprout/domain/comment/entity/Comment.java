package com.example.sprout.domain.comment.entity;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comments")
public class Comment extends BaseTimeEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", updatable = false, nullable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", updatable = false, nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Builder
    public Comment(String content, Member author, Post post, Comment parent) {
        this.content = content;
        this.author = author;
        this.post = post;
        this.parent = parent;
    }

    // 댓글 작성자/요청자 일치 확인
    public boolean isAuthor(Member member) {
        return this.author.getId().equals(member.getId());
    }

    // 댓글 수정
    public void updateComment(String content) {
        this.content = content;
    }
}
