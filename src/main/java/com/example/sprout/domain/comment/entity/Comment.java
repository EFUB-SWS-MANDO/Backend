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
@Table(name = "comments", indexes = @Index(name = "idx_comment_post_id_thread_root_id_id", columnList = "post_id, thread_root_id, id"))
public class Comment extends BaseTimeEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", updatable = false,
                foreignKey = @ForeignKey(
                        name = "fk_comment_author",
                        foreignKeyDefinition = "FOREIGN KEY (author_id) REFERENCES members(id) ON DELETE SET NULL"
                ))
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", updatable = false, nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 최상위 댓글이면 null, 대댓글이면 이 스레드의 최상위 댓글 ID
    @Column(name = "thread_root_id")
    private Long threadRootId;

    @Column(nullable = false)
    private boolean deleted = false;

    @Builder
    public Comment(String content, Member author, Post post, Comment parent) {
        this.content = content;
        this.author = author;
        this.post = post;
        this.parent = parent;
        this.threadRootId = resolveThreadRootId(parent);
    }

    private static Long resolveThreadRootId(Comment parent) {
        if (parent == null) {
            return null;
        }
        // parent도 대댓글일 경우
        return parent.getId();
    }

    // 댓글 작성자/요청자 일치 확인
    public boolean isAuthor(Member member) {
        return this.author.getId().equals(member.getId());
    }

    // 댓글 수정
    public void updateComment(String content) {
        this.content = content;
    }

    // 댓글 삭제
    public void delete() {
        this.deleted = true;
    }
}
