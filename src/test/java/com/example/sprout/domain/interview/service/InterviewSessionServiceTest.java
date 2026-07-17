package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.domain.interview.repository.InterviewAnswerRepository;
import com.example.sprout.domain.interview.repository.InterviewQuestionRepository;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewSessionServiceTest {

    @Mock
    private InterviewSessionRepository interviewSessionRepository;
    @Mock
    private InterviewAnswerRepository interviewAnswerRepository;
    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;

    @InjectMocks
    private InterviewSessionService interviewSessionService;


    @Nested
    @DisplayName("모의면접 단건 삭제")
    class DeleteInterview {

        @Test
        @DisplayName("본인 소유 세션이면 삭제 성공")
        void deleteInterview_success() {
            // given
            Long requesterId = 1L;
            Member requester = mock(Member.class);
            given(requester.getId()).willReturn(requesterId);

            Long interviewId = 10L;
            InterviewSession interviewSession = mock(InterviewSession.class);
            given(interviewSession.getMember()).willReturn(requester);

            given(interviewSessionRepository.findById(interviewId)).willReturn(Optional.of(interviewSession));

            // when
            interviewSessionService.deleteInterview(requesterId, interviewId);

            // then
            verify(interviewAnswerRepository).deleteAllBySession(interviewSession);
            verify(interviewQuestionRepository).deleteAllBySession(interviewSession);
            verify(interviewSessionRepository).delete(interviewSession);
        }

        @Test
        @DisplayName("존재하지 않는 세션이면 INTERVIEW_NOT_FOUND 예외")
        void deleteInterview_sessionNotFound() {
            // given
            Long requesterId = 1L;
            Long nonExistentInterviewSessionId = 2L;
            given(interviewSessionRepository.findById(any(Long.class))).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewSessionService.deleteInterview(requesterId, nonExistentInterviewSessionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND));

            verifyNoInteractions(interviewAnswerRepository, interviewQuestionRepository);
        }

        @Test
        @DisplayName("본인 소유의 세션이 아니면 INTERVIEW_ACCESS_DENIED 예외")
        void deleteInterview_NotOwner() {
            // given
            Long requesterId = 1L;
            Long otherMemberId = 2L;

            Member otherOwner = mock(Member.class);
            given(otherOwner.getId()).willReturn(otherMemberId);

            InterviewSession interviewSession = mock(InterviewSession.class);
            given(interviewSession.getMember()).willReturn(otherOwner);

            Long interviewSessionId = 10L;
            given(interviewSessionRepository.findById(interviewSessionId)).willReturn(Optional.of(interviewSession));

            // when & then
            assertThatThrownBy(() -> interviewSessionService.deleteInterview(requesterId, interviewSessionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(InterviewErrorCode.INTERVIEW_ACCESS_DENIED));
            verifyNoInteractions(interviewAnswerRepository, interviewQuestionRepository);
            verify(interviewSessionRepository, never()).delete(any(InterviewSession.class));
        }


    }

    @Nested
    @DisplayName("회원 모의면접 일괄 삭제")
    class DeleteAllByMember {

        @Test
        @DisplayName("회원의 모든 면접 데이터와 하위 데이터 삭제 성공")
        void deleteAllByMember_success() {
            // given
            Member member = mock(Member.class);
            given(member.getId()).willReturn(1L);

            given(interviewAnswerRepository.deleteAllByMember(member)).willReturn(3);
            given(interviewQuestionRepository.deleteAllByMember(member)).willReturn(3);
            given(interviewSessionRepository.deleteAllByMember(member)).willReturn(1);

            // when
            interviewSessionService.deleteAllByMember(member);

            // then
            verify(interviewAnswerRepository).deleteAllByMember(member);
            verify(interviewQuestionRepository).deleteAllByMember(member);
            verify(interviewSessionRepository).deleteAllByMember(member);
        }
    }
}
