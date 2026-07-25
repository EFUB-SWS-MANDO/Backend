package com.example.sprout.domain.dashboard.service;

import com.example.sprout.domain.dashboard.dto.DashboardResponse;
import com.example.sprout.domain.dashboard.dto.DashboardStatisticsResponse;
import com.example.sprout.domain.dashboard.dto.RecentActivityResponse;
import com.example.sprout.domain.dashboard.enums.ActivityType;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.enums.InterviewSessionType;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.motivation.entity.Motivation;
import com.example.sprout.domain.motivation.service.MotivationService;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private MotivationService motivationService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private InterviewSessionRepository interviewSessionRepository;
    @Mock
    private ResumeRepository resumeRepository;

    @InjectMocks
    private DashboardService dashboardService;


    private static final Long MEMBER_ID = 1L;

    // === 공통 fixture ===

    private Member createMember(int visitStreak) {
        Member member = Member.builder()
                .oauthId("test-id")
                .oauthProvider(OauthProvider.KAKAO)
                .build();
        ReflectionTestUtils.setField(member, "visitStreak", visitStreak);
        return member;
    }

    private Motivation createMotivation(String content) {
        return Motivation.builder()
                .content(content)
                .displayOrder(1)
                .build();
    }

    private Post mockPost(String title, LocalDateTime updatedAt) {
        Post post = mock(Post.class);
        given(post.getTitle()).willReturn(title);
        given(post.getUpdatedAt()).willReturn(updatedAt);
        return post;
    }

    private InterviewSession mockInterviewSession(String title, LocalDateTime updatedAt) {
        InterviewSession interviewSession = mock(InterviewSession.class);
        given(interviewSession.getTitle()).willReturn(title);
        given(interviewSession.getUpdatedAt()).willReturn(updatedAt);
        return interviewSession;
    }

    private Resume mockResume(String title, LocalDateTime updatedAt) {
        Resume resume = mock(Resume.class);
        given(resume.getTitle()).willReturn(title);
        given(resume.getUpdatedAt()).willReturn(updatedAt);
        return resume;
    }

    @Nested
    @DisplayName("대시보드 조회")
    class GetDashboard {

        @Test
        @DisplayName("동기부여 문구, 통계, 최근 활동(최신순 정렬) 반환 성공")
        void returnDashboardWithAllFields() {
            // given
            LocalDateTime now = LocalDateTime.now();
            String motivationContent = "동기부여 문구";

            Member requester = createMember(1);
            Motivation motivation = createMotivation(motivationContent);

            Post post = mockPost("게시글", now);
            InterviewSession interviewSession = mockInterviewSession("모의면접", now.minusHours(1));
            Resume resume = mockResume("자소서", now.minusHours(2));

            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(requester));
            given(motivationService.getDailyMotivation()).willReturn(motivation);

            given(postRepository.countAllByAuthor(requester)).willReturn(1);
            // TODO: PostLikeRepository 구현 PR 머지 후 테스트 코드 추가
            given(interviewSessionRepository.countAllByMember(requester)).willReturn(1);
            given(resumeRepository.countAllByAuthor(requester)).willReturn(1);

            given(postRepository.findTop4ByAuthorOrderByUpdatedAtDesc(requester)).willReturn(List.of(post));
            given(interviewSessionRepository.findTop4ByMemberOrderByUpdatedAtDesc(requester)).willReturn(List.of(interviewSession));
            given(resumeRepository.findTop4ByAuthorOrderByUpdatedAtDesc(requester)).willReturn(List.of(resume));

            // when
            DashboardResponse response = dashboardService.getDashboard(MEMBER_ID);

            // then
            assertThat(response.motivation()).isEqualTo(motivationContent);
            assertThat(response.statistics()).isInstanceOf(DashboardStatisticsResponse.class);
            assertThat(response.statistics().attendanceStreak()).isEqualTo(1);
            assertThat(response.recentActivities()).hasSize(3);
            assertThat(response.recentActivities()).extracting(RecentActivityResponse::type)
                    .containsExactly(ActivityType.POST, ActivityType.INTERVIEW, ActivityType.RESUME);
        }

        @Test
        @DisplayName("전체 활동이 4개를 초과하면 최신 4개만 반환")
        void limitToFourItems() {
            LocalDateTime now = LocalDateTime.now();
            String motivationContent = "동기부여 문구";

            Member requester = createMember(1);
            Motivation motivation = createMotivation(motivationContent);

            Post post1 = mockPost("게시글", now);
            Post post2 = mockPost("게시글", now.minusHours(1));
            Post post3 = mockPost("게시글", now.minusHours(2));
            Post post4 = mockPost("게시글", now.minusHours(3));
            InterviewSession interviewSession = mockInterviewSession("모의면접", now.minusHours(4));


            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(requester));
            given(motivationService.getDailyMotivation()).willReturn(motivation);

            given(postRepository.countAllByAuthor(requester)).willReturn(1);
            // TODO: PostLikeRepository 구현 PR 머지 후 테스트 코드 추가
            given(interviewSessionRepository.countAllByMember(requester)).willReturn(1);
            given(resumeRepository.countAllByAuthor(requester)).willReturn(1);

            given(postRepository.findTop4ByAuthorOrderByUpdatedAtDesc(requester)).willReturn(List.of(post1, post2, post3, post4));
            given(interviewSessionRepository.findTop4ByMemberOrderByUpdatedAtDesc(requester)).willReturn(List.of(interviewSession));
            given(resumeRepository.findTop4ByAuthorOrderByUpdatedAtDesc(requester)).willReturn(List.of());

            // when
            DashboardResponse response = dashboardService.getDashboard(MEMBER_ID);

            // then
            assertThat(response.recentActivities()).hasSize(4);
            assertThat(response.recentActivities()).extracting(RecentActivityResponse::type)
                    .doesNotContain(ActivityType.INTERVIEW);
        }

    }
}
