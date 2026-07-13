package com.example.sprout.domain.resume.repository;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    void deleteAllByAuthor (Member author);
    List<Resume> findAllByAuthor(Member member);
}
