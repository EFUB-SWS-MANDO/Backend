package com.example.sprout.domain.interview.controller;

import com.example.sprout.domain.auth.security.AuthMemberResolver;
import com.example.sprout.domain.auth.security.CustomUserDetails;
import com.example.sprout.domain.interview.dto.response.InterviewSessionCursorResponse;
import com.example.sprout.domain.interview.dto.response.InterviewSessionSummaryResponse;
import com.example.sprout.domain.interview.service.InterviewSessionService;
import com.example.sprout.global.error.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InterviewSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalExceptionHandler.class,
})
public class InterviewSessionControllerTest {

    private static final Long REQUESTER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterviewSessionService interviewSessionService;

    @TestConfiguration
    static class AuthMemberResolverConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            // 실제 프로덕션 리졸버 그대로 등록
            resolvers.add(new AuthMemberResolver());
        }
    }

    private static RequestPostProcessor authenticatedAs(Long memberId) {
        return request -> {
            CustomUserDetails userDetails = mock(CustomUserDetails.class);
            given(userDetails.getMemberId()).willReturn(memberId);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return request;
        };
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/interviews - 모의면접 목록 조회")
    class GetInterviews {

        @Test
        @DisplayName("정상 요청이면 200과 목록 반환")
        void getInterviews_success() throws Exception {
            // given
            InterviewSessionSummaryResponse summary = new InterviewSessionSummaryResponse(
                    1L, "테스트", null, null, null
            );
            InterviewSessionCursorResponse response =  new InterviewSessionCursorResponse(
                    List.of(summary), null, false, 1L
            );
            given(interviewSessionService.getInterviews(REQUESTER_ID, null, 10))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/interviews").with(authenticatedAs(REQUESTER_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.interviews.length()").value(1))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("idAfter, limit 파라미터를 그대로 서비스에 전달")
        void getInterviews_withParams() throws Exception {
            // given
            InterviewSessionCursorResponse response = new InterviewSessionCursorResponse(
                    List.of(), null, false, 0L
            );
            given(interviewSessionService.getInterviews(REQUESTER_ID, 5L, 20))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/interviews").with(authenticatedAs(REQUESTER_ID))
                    .param("idAfter", "5")
                    .param("limit", "20"))
                    .andExpect(status().isOk());
            verify(interviewSessionService).getInterviews(REQUESTER_ID, 5L, 20);
        }

        @Test
        @DisplayName("limit 파라미터가 없으면 기본값 10 사용")
        void getInterviews_defaultLimit() throws Exception {
            // given
            InterviewSessionCursorResponse response = new InterviewSessionCursorResponse(
                    List.of(), null, false, 0L
            );
            given(interviewSessionService.getInterviews(REQUESTER_ID, null, 10))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/interviews").with(authenticatedAs(REQUESTER_ID)))
                    .andExpect(status().isOk());
            verify(interviewSessionService).getInterviews(REQUESTER_ID, null, 10);
        }

        @ParameterizedTest(name = "limit={0}이면 400을 반환")
        @ValueSource(ints = {0, 101})
        void getInterviews_invalidLimit(int limit) throws Exception {
            // when & then
            mockMvc.perform(get("/api/interviews").with(authenticatedAs(REQUESTER_ID))
                    .param("limit", String.valueOf(limit)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("idAfter가 0 이하면 400을 반환")
        void getInterviews_invalidIdAfter() throws Exception {
            // when & then
            mockMvc.perform(get("/api/interviews").with(authenticatedAs(REQUESTER_ID))
                            .param("idAfter", "0"))
                    .andExpect(status().isBadRequest());
        }

    }
}
