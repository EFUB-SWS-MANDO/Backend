package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.resume.dto.request.GetResumeListCondition;
import com.example.sprout.domain.resume.dto.response.GetResumeListResponse;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class ResumeServiceGetResumeListTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private ResumeService resumeService;

    private Long requesterId;
    private Member author;

    @BeforeEach
    void setUp() {
        requesterId = 1L;
        author = mock(Member.class);
    }

    @Nested
    @DisplayName("getResumeList - 자소서 목록 조회")
    class GetResumeList {

        @Test
        @DisplayName("자소서 목록 조회 성공")
        void getResumeList_success() {
            // given
            GetResumeListCondition condition = new GetResumeListCondition("키워드", null, 10);

            given(memberRepository.findById(requesterId))
                    .willReturn(Optional.of(author));

            Resume resume1 = mock(Resume.class);
            Resume resume2 = mock(Resume.class);

            given(resumeRepository.findPageByAuthorAndKeyword(
                    eq(author), eq(condition.idAfter()), eq(condition.keyword()), any(Pageable.class)))
                    .willReturn(List.of(resume1, resume2));

            given(resumeRepository.countAllByAuthorAndKeyword(author, condition.keyword()))
                    .willReturn(2L);

            // when
            GetResumeListResponse response = resumeService.getResumeList(requesterId, condition);

            // then
            assertThat(response.resumes()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.totalElements()).isEqualTo(2L);
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 MEMBER_NOT_FOUND 예외를 던진다")
        void throwsWhenMemberNotFound() {
            // given
            GetResumeListCondition condition = new GetResumeListCondition(null, null, 2);
            given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> resumeService.getResumeList(1L, condition))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);

            verifyNoInteractions(resumeRepository);
        }

        @Test
        @DisplayName("조회 결과가 limit보다 많으면 hasNext=true, limit개만 반환, nextIdAfter는 마지막 항목의 id")
        void hasNextTrueWhenMoreThanLimit() {
            Long requesterId = 1L;
            int limit = 2;
            GetResumeListCondition condition = new GetResumeListCondition(null, null, 2);
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(author));

            Resume r1 = resumeWithId(3L, "세번째");
            Resume r2 = resumeWithId(2L, "두번째");
            Resume r3 = mock(Resume.class); // stub 없이 개수만 채우는 용도 (어차피 trim되어 안 쓰임)

            given(resumeRepository.findPageByAuthorAndKeyword(any(), any(), any(), any()))
                    .willReturn(List.of(r1, r2, r3));
            given(resumeRepository.countAllByAuthorAndKeyword(any(), any())).willReturn(5L);

            GetResumeListResponse response = resumeService.getResumeList(requesterId, condition);

            assertThat(response.resumes()).hasSize(limit);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextIdAfter()).isEqualTo(2L);
            assertThat(response.totalElements()).isEqualTo(5L);
        }

        @Test
        @DisplayName("조회 결과가 limit 이하면 hasNext=false, nextIdAfter는 null")
        void hasNextFalseWhenWithinLimit() {
            // given
            Long requesterId = 1L;
            GetResumeListCondition condition = new GetResumeListCondition(null, null, 10);
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(author));

            Resume onlyResume = resumeWithId(1L, "유일한 글");   // 먼저 만들고
            given(resumeRepository.findPageByAuthorAndKeyword(any(), any(), any(), any()))
                    .willReturn(List.of(onlyResume));           // 그 다음 stubbing
            given(resumeRepository.countAllByAuthorAndKeyword(any(), any())).willReturn(1L);

            // when
            GetResumeListResponse response = resumeService.getResumeList(requesterId, condition);

            // then
            assertThat(response.resumes()).hasSize(1);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextIdAfter()).isNull();
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 리스트와 hasNext=false, nextIdAfter=null을 반환한다")
        void emptyResult() {
            // given
            Long requesterId = 1L;
            GetResumeListCondition condition = new GetResumeListCondition(null, null, 10);
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(author));
            given(resumeRepository.findPageByAuthorAndKeyword(any(), any(), any(), any()))
                    .willReturn(List.of());
            given(resumeRepository.countAllByAuthorAndKeyword(any(), any())).willReturn(0L);

            // when
            GetResumeListResponse response = resumeService.getResumeList(requesterId, condition);

            // then
            assertThat(response.resumes()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextIdAfter()).isNull();
            assertThat(response.totalElements()).isZero();
        }

        @Test
        @DisplayName("keyword가 그대로 repository 조회 조건에 전달된다")
        void keywordPassedToRepository() {
            // given
            Long requesterId = 1L;
            String keyword = "Spring";
            GetResumeListCondition condition = new GetResumeListCondition(keyword, 10L, 10);
            given(memberRepository.findById(requesterId)).willReturn(Optional.of(author));
            given(resumeRepository.findPageByAuthorAndKeyword(any(), any(), any(), any()))
                    .willReturn(List.of());
            given(resumeRepository.countAllByAuthorAndKeyword(any(), any())).willReturn(0L);

            // when
            resumeService.getResumeList(requesterId, condition);

            // then
            org.mockito.Mockito.verify(resumeRepository)
                    .findPageByAuthorAndKeyword(author, 10L, keyword, org.springframework.data.domain.PageRequest.of(0, 11));
            org.mockito.Mockito.verify(resumeRepository)
                    .countAllByAuthorAndKeyword(author, keyword);
        }
    }

    private Resume resumeWithId(Long id, String title) {
        Resume resume = mock(Resume.class);
        given(resume.getId()).willReturn(id);
        given(resume.getTitle()).willReturn(title);
        return resume;
    }
}
