package com.example.sprout.domain.interview.repository;

import com.example.sprout.domain.interview.entity.InterviewSession;
import com.example.sprout.domain.interview.enums.InterviewSessionType;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.enums.OauthProvider;
import com.example.sprout.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class InterviewSessionRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private InterviewSessionRepository interviewSessionRepository;

    @Autowired
    private TestEntityManager em;

    Member member1;

    @BeforeEach
    void setUp() {
        member1 = em.persistAndFlush(buildMember("1"));
    }

    @Nested
    @DisplayName("findCursorPageByMemberId")
    class FindCursorPageByMemberId {

        @Test
        @DisplayName("idAfter가 null이면 id 내림차순으로 첫 페이지를 조회")
        void findFirstPage() {
            // given
            InterviewSession session1 = em.persistAndFlush(buildInterviewSession(member1, InterviewSessionType.POST));
            InterviewSession session2 = em.persistAndFlush(buildInterviewSession(member1, InterviewSessionType.POST));
            InterviewSession session3 = em.persistAndFlush(buildInterviewSession(member1, InterviewSessionType.POST));

            // when
            List<InterviewSession> result = interviewSessionRepository.findCursorPageByMemberId(
                   member1.getId() ,null, Pageable.ofSize(3)
            );

            // then
            assertThat(result).extracting(InterviewSession::getId)
                    .containsExactly(session3.getId(), session2.getId(), session1.getId());
        }

        @Test
        @DisplayName("idAfter가 주어지면 해당 id보다 작은 데이터만 조회")
        void findWithCursor() {
            // given
            InterviewSession session1 = em.persistAndFlush(buildInterviewSession(member1, InterviewSessionType.POST));
            InterviewSession session2 = em.persistAndFlush(buildInterviewSession(member1, InterviewSessionType.POST));
            InterviewSession session3 = em.persistAndFlush(buildInterviewSession(member1, InterviewSessionType.POST));

            // when
            List<InterviewSession> result = interviewSessionRepository.findCursorPageByMemberId(
                    member1.getId(), session3.getId(), Pageable.ofSize(10)
            );

            // then
            assertThat(result).extracting(InterviewSession::getId)
                    .containsExactly(session2.getId(), session1.getId());
        }
    }

    // Text Fixture
    private Member buildMember(String oauthId) {
        return Member.builder()
                .oauthId(oauthId)
                .oauthProvider(OauthProvider.KAKAO)
                .build();
    }

    private InterviewSession buildInterviewSession(Member member, InterviewSessionType type) {
        return InterviewSession.builder()
                .member(member)
                .title("테스트")
                .type(type)
                .build();
    }
}
