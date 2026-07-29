package com.example.sprout.domain.resume.entity;

import java.io.Serializable;
import java.util.Objects;

public class ResumeSourcePostId implements Serializable {

    private Long resume;
    private Long post;

    public ResumeSourcePostId() {}

    public ResumeSourcePostId(Long resume, Long post) {
        this.resume = resume;
        this.post = post;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResumeSourcePostId that)) return false;
        return Objects.equals(resume, that.resume) && Objects.equals(post, that.post);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resume, post);
    }
}
