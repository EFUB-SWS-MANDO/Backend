package com.example.sprout.domain.resume.entity;

import com.example.sprout.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "resume_source_posts")
@IdClass(ResumeSourcePostId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeSourcePost {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder
    public ResumeSourcePost(Resume resume, Post post) {
        this.resume = resume;
        this.post = post;
    }
}
