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
public class Comment extends BaseTimeEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", updatable = false)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Comment> children = new ArrayList<>();

    @Builder
    public Comment(String content, Member author, Post post, Comment parent) {
        this.content = content;
        this.author = author;
        this.post = post;
        this.parent = parent;
    }

    public void addChild(Comment child) {
        children.add(child);
        child.parent = this;
    }
}
