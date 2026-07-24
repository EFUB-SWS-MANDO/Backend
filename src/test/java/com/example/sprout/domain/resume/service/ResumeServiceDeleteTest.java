package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.resume.entity.Resume;
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
import org.testcontainers.shaded.com.google.common.reflect.Reflection;

import javax.swing.text.html.Option;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ResumeServiceDeleteTest {

    @InjectMocks
    private ResumeService resumeService;

    @Mock
    MemberRepository memberRepository;

    @Mock
    ResumeRepository resumeRepository;

    @Mock
    ResumeDraftService resumeDraftService;

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

    private Resume createResumeFixture(Member author) {
        Resume resume = Resume.builder()
                .author(author)
                .title("백엔드 신입 자소서")
                .build();
        ReflectionTestUtils.setField(resume, "id", 100L);
        return resume;
    }

    @Test
    @DisplayName("자소서 삭제 성공 - 본인 자소서 삭제")
    void deleteResume_success() {
        // given
        Resume resume = createResumeFixture(author);

        given(memberRepository.findById(1L)).willReturn(Optional.of(author));
        given(resumeRepository.findById(100L)).willReturn(Optional.of(resume));

        // when
        resumeService.deleteResume(1L, 100L);

        // then
        verify(resumeRepository).delete(resume);
    }

    @Test
    @DisplayName("자소서 삭제 실패 - 존재하지 않는 회원")
    void deleteResume_fail_memberNotFound () {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> resumeService.deleteResume(999L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);

        verify(resumeRepository, never()).findById(anyLong());
        verify(resumeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("자소서 삭제 실패 - 존재하지 않는 자소서")
    void deleteResume_fail_resumeNotFound () {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(author));
        given(resumeRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> resumeService.deleteResume(1L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ResumeErrorCode.RESUME_NOT_FOUND);

        verify(resumeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("자소서 삭제 실패 - 타인의 자소서 삭제 시도 (IDOR 방지)")
    void deleteResume_fail_accessDenied () {
        // given
        Resume resume = createResumeFixture(author);

        given(memberRepository.findById(2L)).willReturn(Optional.of(stranger));
        given(resumeRepository.findById(100L)).willReturn(Optional.of(resume));

        // when & then
        assertThatThrownBy(() -> resumeService.deleteResume(2L, 100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ResumeErrorCode.RESUME_ACCESS_DENIED);

        verify(resumeRepository, never()).delete(any());
    }
}
