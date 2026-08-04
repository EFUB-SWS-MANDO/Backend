package com.example.sprout.domain.resume.repository;

import com.example.sprout.domain.resume.entity.ResumeSourcePost;
import com.example.sprout.domain.resume.entity.ResumeSourcePostId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResumeSourcePostRepository extends JpaRepository<ResumeSourcePost, ResumeSourcePostId> {

    List<ResumeSourcePost> findAllByResumeId(Long resumeId);

    @Modifying
    @Query("DELETE FROM ResumeSourcePost rsp WHERE rsp.resume.id = :resumeId")
    void deleteAllByResumeId(Long resumeId);

    @Modifying
    @Query("DELETE FROM ResumeSourcePost rsp WHERE rsp.post.id = :postId")
    void deleteAllByPostId(Long postId);
}
