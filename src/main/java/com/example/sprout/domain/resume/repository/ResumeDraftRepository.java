package com.example.sprout.domain.resume.repository;

import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.entity.ResumeDraft;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeDraftRepository extends JpaRepository<ResumeDraft, Long> {
    void deleteAllByResume(Resume resume);
}
