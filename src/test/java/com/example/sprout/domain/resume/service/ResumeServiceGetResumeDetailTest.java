package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.resume.dto.response.ResumeResponse;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.entity.ResumeDraft;
import com.example.sprout.domain.resume.exception.ResumeErrorCode;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResumeServiceGetResumeDetailTest {

    @InjectMocks
    private ResumeService resumeService;

    @Mock private MemberRepository memberRepository;
    @Mock private ResumeRepository resumeRepository;

    private Member author;
    private Member stranger;

    @BeforeEach
    void setUp() {
        author = Member.builder()
                .oauthId("12345")
                .oauthProvider(OauthProvider.KAKAO)
                .build();
        ReflectionTestUtils.setField(author, "id", 1L);

        stranger = Member.builder()
                .oauthId("23456")
                .oauthProvider(OauthProvider.KAKAO)
                .build();
        ReflectionTestUtils.setField(stranger, "id", 2L);
    }

    private Resume createResumeFixture(Member author, ResumeDraft... drafts) {
        Resume resume = Resume.builder()
                .author(author)
                .title("백엔드 신입 자소서")
                .build();
        ReflectionTestUtils.setField(resume, "id", 100L);
        ReflectionTestUtils.setField(resume, "createdAt", LocalDateTime.now());
        for (ResumeDraft draft : drafts) {
            resume.getResumeDraftList().add(draft);
        }
        return resume;
    }

    private ResumeDraft createDraftFixture(Long id, Long order, String question) {
        ResumeDraft draft = ResumeDraft.builder()
                .orderIndex(order)
                .question(question)
                .answer("답변 내용")
                .description("답변 설명")
                .build();
        ReflectionTestUtils.setField(draft, "id", id);
        return draft;
    }

    @Test
    @DisplayName("자소서 상세 조회 성공 - 본인 자소서 조회")
    void getResumeDetail_success() {
        // given
        ResumeDraft draft1 = createDraftFixture(1L, 1L, "지원 동기는?");
        ResumeDraft draft2 = createDraftFixture(2L, 2L, "성장 과정은?");
        Resume resume = createResumeFixture(author, draft1, draft2);

        given(memberRepository.findById(1L)).willReturn(Optional.of(author));
        given(resumeRepository.findById(100L)).willReturn(Optional.of(resume));

        // when
        ResumeResponse response = resumeService.getResumeDetail(1L, 100L);

        // then
        assertThat(response.resumeId()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("백엔드 신입 자소서");
        assertThat(response.questions()).hasSize(2);
        assertThat(response.questions())
                .extracting("content")
                .containsExactly("지원 동기는?", "성장 과정은?");
    }

    @Test
    @DisplayName("자소서 상세 조회 성공 - 문항이 없는 경우 빈 리스트 반환")
    void getResumeDetail_success_emptyDrafts() {
        // given
        Resume resume = createResumeFixture(author);

        given(memberRepository.findById(1L)).willReturn(Optional.of(author));
        given(resumeRepository.findById(100L)).willReturn(Optional.of(resume));

        // when
        ResumeResponse response = resumeService.getResumeDetail(1L, 100L);

        // then
        assertThat(response.questions()).isEmpty();
    }

    @Test
    @DisplayName("자소서 상세 조회 실패 - 존재하지 않는 회원")
    void getResumeDetail_fail_memberNotFound() {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> resumeService.getResumeDetail(999L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

        verify(resumeRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("자소서 상세 조회 실패 - 존재하지 않는 자소서")
    void getResumeDetail_fail_resumeNotFound() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(author));
        given(resumeRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> resumeService.getResumeDetail(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ResumeErrorCode.RESUME_NOT_FOUND);
    }

    @Test
    @DisplayName("자소서 상세 조회 실패 - 타인의 자소서 접근 시도")
    void getResumeDetail_fail_accessDenied() {
        // given
        Resume resume = createResumeFixture(author);

        given(memberRepository.findById(2L)).willReturn(Optional.of(stranger));
        given(resumeRepository.findById(100L)).willReturn(Optional.of(resume));

        // when & then
        assertThatThrownBy(() -> resumeService.getResumeDetail(2L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ResumeErrorCode.RESUME_ACCESS_DENIED);
    }
}