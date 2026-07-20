package com.example.sprout.domain.interview.service;

import com.example.sprout.domain.interview.dto.response.InterviewFeedbackResponse;
import com.example.sprout.domain.interview.dto.response.InterviewSessionCursorResponse;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.domain.interview.repository.InterviewAnswerRepository;
import com.example.sprout.domain.interview.repository.InterviewQuestionRepository;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.global.error.BusinessException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("모의면접 목록 조회")
    class GetInterviews {

        @Test
        @DisplayName("다음 페이지가 없으면 hasNext=false, nextIdAfter=null 반환")
        void getInterviews_noNextPage() {
            //given
            Long requesterId = 1L;

            InterviewSession session1 = mock(InterviewSession.class);
            given(session1.getId()).willReturn(3L);
            InterviewSession session2 = mock(InterviewSession.class);
            given(session2.getId()).willReturn(2L);

            given(interviewSessionRepository.findCursorPageByMemberId(eq(requesterId), isNull(), any(Pageable.class)))
                    .willReturn(List.of(session1, session2));
            given(interviewSessionRepository.countAllByMemberId(requesterId)).willReturn(2L);

            // when
            InterviewSessionCursorResponse response = interviewSessionService
                    .getInterviews(requesterId, null, 10);

            // then
            assertThat(response.interviews()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextIdAfter()).isNull();
            assertThat(response.totalElements()).isEqualTo(2L);
        }

        @Test
        @DisplayName("조회 결과가 limit을 초과하면 hasNext=true, 마지막 요소 id를 nextIdAfter로 반환")
        void getInterviews_hasNextPage() {
            //given
            int limit = 2;
            Long requesterId = 1L;

            InterviewSession session1 = mock(InterviewSession.class);
            given(session1.getId()).willReturn(4L);
            InterviewSession session2 = mock(InterviewSession.class);
            given(session2.getId()).willReturn(3L);
            InterviewSession session3 = mock(InterviewSession.class);

            given(interviewSessionRepository.findCursorPageByMemberId(eq(requesterId), isNull(), any(Pageable.class)))
                    .willReturn(List.of(session1, session2, session3));
            given(interviewSessionRepository.countAllByMemberId(requesterId)).willReturn(10L);

            // when
            InterviewSessionCursorResponse response = interviewSessionService.getInterviews(requesterId, null, limit);

            // then
            assertThat(response.interviews()).hasSize(limit);
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextIdAfter()).isEqualTo(3L);
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 리스트와 hasNext=false 반환")
        void getInterviews_empty() {
            // given
            Long requesterId = 1L;

            given(interviewSessionRepository.findCursorPageByMemberId(eq(requesterId), isNull(), any(Pageable.class)))
                    .willReturn(List.of());
            given(interviewSessionRepository.countAllByMemberId(requesterId)).willReturn(0L);

            // when
            InterviewSessionCursorResponse response = interviewSessionService.getInterviews(requesterId, null, 10);

            // then
            assertThat(response.interviews()).isEmpty();
            assertThat(response.hasNext()).isFalse();
            assertThat(response.totalElements()).isZero();
        }

        @Test
        @DisplayName("idAfter 커서를 리포지토리에 그대로 전달")
        void getInterviews_withIdAfter() {
            // given
            Long requesterId = 1L;
            Long idAfter = 100L;

            given(interviewSessionRepository.findCursorPageByMemberId(eq(requesterId), eq(idAfter), any(Pageable.class)))
                    .willReturn(List.of());
            given(interviewSessionRepository.countAllByMemberId(requesterId)).willReturn(0L);

            // when
            interviewSessionService.getInterviews(requesterId, idAfter, 10);

            // then
            verify(interviewSessionRepository).findCursorPageByMemberId(eq(requesterId), eq(idAfter), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("모의면접 총평 조회")
    class GetFeedback {

        @Test
        @DisplayName("본인 소유 세션이면 총평 조회 성공")
        void getFeedback_success() {
            // given
            Long requesterId = 1L;
            Member requester = mock(Member.class);
            given(requester.getId()).willReturn(requesterId);

            Long interviewSessionId = 10L;
            String feedback = "모의면접 총평";
            String feedbackSummary = "모의면접 총평 요약";
            InterviewSession interviewSession = mock(InterviewSession.class);
            given(interviewSession.getMember()).willReturn(requester);
            given(interviewSession.getFeedback()).willReturn(feedback);
            given(interviewSession.getFeedbackSummary()).willReturn(feedbackSummary);
            given(interviewSession.hasFeedback()).willReturn(true);

            given(interviewSessionRepository.findById(interviewSessionId)).willReturn(Optional.of(interviewSession));

            // when
            InterviewFeedbackResponse response = interviewSessionService.getFeedback(requesterId, interviewSessionId);

            // then
            verify(interviewSessionRepository).findById(interviewSessionId);
            assertThat(response).isInstanceOf(InterviewFeedbackResponse.class);
            assertThat(response.feedback()).isEqualTo(feedback);
            assertThat(response.feedbackSummary()).isEqualTo(feedbackSummary);
        }

        @Test
        @DisplayName("존재하지 않는 세션이면 INTERVIEW_NOT_FOUND 예외")
        void getFeedback_sessionNotFound() {
            // given
            Long requesterId = 1L;
            Long nonExistentInterviewSessionId = 10L;

            given(interviewSessionRepository.findById(nonExistentInterviewSessionId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> interviewSessionService.getFeedback(requesterId, nonExistentInterviewSessionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND));
        }

        @Test
        @DisplayName("본인 소유의 세션이 아니면 INTERVIEW_ACCESS_DENIED 예외")
        void getFeedback_NotOwner() {
            // given
            Long requesterId = 1L;

            Long otherMemberId = 2L;
            Member otherMember = mock(Member.class);
            given(otherMember.getId()).willReturn(otherMemberId);

            Long interviewSessionId = 10L;
            InterviewSession interviewSession = mock(InterviewSession.class);
            given(interviewSession.getMember()).willReturn(otherMember);

            given(interviewSessionRepository.findById(interviewSessionId)).willReturn(Optional.of(interviewSession));

            // when & then
            assertThatThrownBy(() -> interviewSessionService.getFeedback(requesterId, interviewSessionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(InterviewErrorCode.INTERVIEW_ACCESS_DENIED));
        }

        @Test
        @DisplayName("총평이 존재하지 않으면 INTERVIEW_FEEDBACK_NOT_FOUND 예외")
        void getFeedback_feedbackNotFound() {
            // given
            Long requesterId = 1L;
            Member requester = mock(Member.class);
            given(requester.getId()).willReturn(requesterId);

            Long interviewSessionId = 10L;
            InterviewSession interviewSession = mock(InterviewSession.class);
            given(interviewSession.getMember()).willReturn(requester);
            given(interviewSession.hasFeedback()).willReturn(false);

            given(interviewSessionRepository.findById(interviewSessionId)).willReturn(Optional.of(interviewSession));

            // when & then
            assertThatThrownBy(() -> interviewSessionService.getFeedback(requesterId, interviewSessionId))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(InterviewErrorCode.INTERVIEW_FEEDBACK_NOT_FOUND));
        }
    }

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
