package com.example.sprout.domain.dashboard.service;

import com.example.sprout.domain.dashboard.dto.DashboardResponse;
import com.example.sprout.domain.dashboard.dto.DashboardStatisticsResponse;
import com.example.sprout.domain.dashboard.dto.RecentActivityResponse;
import com.example.sprout.domain.dashboard.enums.ActivityType;
import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.repository.InterviewSessionRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.motivation.service.MotivationService;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final MotivationService motivationService;

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final ResumeRepository resumeRepository;


    public DashboardResponse getDashboard(Long requesterId) {
        Member requester = getMember(requesterId);

        return DashboardResponse.of(
                motivationService.getDailyMotivation(), // 오늘의 동기부여 문구
                getStatistics(requester), // 게시글 수, 좋아요 수, 연속 접속일, 모의면접 수, 자소서 수
                getRecentActivities(requester) // 최근 활동 4개 (게시글/모의면접/자소서 중)
        );
    }

    // === Helper Method ===
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    // 통계, 기록 (게시글 수, 좋아요 수, 연속 접속일, 모의면접 수, 자소서 수) DTO 반환
    private DashboardStatisticsResponse getStatistics(Member member) {
        int postCount = postRepository.countAllByAuthor(member);
        int likeCount = 1; // TODO: 추후 PostLikeRepository 구현 PR 머지 후 구현
        int loginStreak = member.getVisitStreak();
        int interviewCount = interviewSessionRepository.countAllByMember(member);
        int resumeCount = resumeRepository.countAllByAuthor(member);

        return DashboardStatisticsResponse.of(
                postCount, likeCount, loginStreak, interviewCount, resumeCount
        );
    }

    // 게시글, 자소서, 모의면접 통합한 모든 활동 중 최신순으로 4개 반환
    private List<RecentActivityResponse> getRecentActivities(Member member) {
        List<Post> posts = postRepository.findTop4ByAuthorOrderByUpdatedAtDesc(member);
        List<Resume> resumes = resumeRepository.findTop4ByAuthorOrderByUpdatedAtDesc(member);
        List<InterviewSession> interviewSessions = interviewSessionRepository.findTop4ByMemberOrderByUpdatedAtDesc(member);

        // 게시글, 자소서, 모의면접은 서로 다른 도메인이므로 각각 조회 후 최근 활동 기준으로 통합
        return Stream.of(
                posts.stream().map(p -> RecentActivityResponse.of(ActivityType.POST, p)),
                interviewSessions.stream().map(i -> RecentActivityResponse.of(ActivityType.INTERVIEW, i)),
                resumes.stream().map(r -> RecentActivityResponse.of(ActivityType.RESUME, r))
                )
                .flatMap(stream -> stream)
                .sorted(Comparator.comparing(RecentActivityResponse::updatedAt).reversed())
                .limit(4)
                .toList();
    }

}
