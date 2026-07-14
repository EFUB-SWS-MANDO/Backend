package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.repository.ResumeDraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeDraftService {
    private final ResumeDraftRepository resumeDraftRepository;

    @Transactional
    public void deleteAllByResume(Resume resume) {
        resumeDraftRepository.deleteAllByResume(resume);
    }
}
