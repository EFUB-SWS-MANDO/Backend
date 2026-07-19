package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.exception.ResumeErrorCode;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final MemberRepository memberRepository;
    private final ResumeRepository resumeRepository;

    private final ResumeDraftService resumeDraftService;

    // 자소서 삭제
    @Transactional
    public void deleteResume(Long requesterId, Long resumeId) {
        Member requester = getMember(requesterId);
        Resume resume = getResume(resumeId);

        validateAuthor(requesterId, resume.getAuthor());

        resumeRepository.delete(resume);
        log.info("자소서 삭제 완료 - resumeId: {}, requesterId: {}", resumeId, requesterId);
    }

    //회원 탈퇴 시 resume 및 resumeDraft 삭제
    @Transactional
    public void deleteByMember(Member member) {
        //resumeDraft 삭제
        List<Resume> resumeList = resumeRepository.findAllByAuthor(member);
        resumeList.forEach(resumeDraftService::deleteAllByResume);

        //resume 삭제
        resumeRepository.deleteAllByAuthor(member);
    }


    // Member 조회
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 회원 - memberId: {}", memberId);
                    return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
    }

    // Resume 조회
    private Resume getResume(Long resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 자기소개서 - resumeId: {}", resumeId);
                    return new BusinessException(ResumeErrorCode.RESUME_NOT_FOUND);
                });
    }

    // Author == Requester
    private void validateAuthor(Long requesterId, Member author) {
        if (!requesterId.equals(author.getId())) {
            throw new BusinessException(ResumeErrorCode.RESUME_ACCESS_DENIED);
        }
    }
}
