package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;

    private final ResumeDraftService resumeDraftService;

    //회원 탈퇴 시 resume 및 resumeDraft 삭제
    @Transactional
    public void deleteByMember(Member member) {
        //resumeDraft 삭제
        List<Resume> resumeList = resumeRepository.findAllByAuthor(member);
        resumeList.forEach(resumeDraftService::deleteAllByResume);

        //resume 삭제
        resumeRepository.deleteAllByAuthor(member);
    }
}
