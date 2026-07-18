package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.resume.dto.ai.GeneratedAnswer;
import com.example.sprout.domain.resume.dto.request.CreateResumeRequest;
import com.example.sprout.domain.resume.dto.response.ResumeResponse;
import com.example.sprout.domain.resume.exception.ResumeErrorCode;
import com.example.sprout.domain.resume.parser.ResumeAiResponseParser;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import com.example.sprout.global.ai.client.AiChatClient;
import com.example.sprout.global.ai.dto.AiChatResponse;
import com.example.sprout.global.ai.prompt.PromptTemplateLoader;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResumeServiceCreateTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeDraftService resumeDraftService;

    @Mock
    private AiChatClient aiChatClient;

    @Mock
    private PromptTemplateLoader promptTemplateLoader;

    @Mock
    private PromptTemplateLoader.PromptTemplate promptTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResumeAiResponseParser parser;

    @InjectMocks
    private ResumeService resumeService;

    private Long requesterId;
    private Member member;
    private Post post;
    private CreateResumeRequest request;

    @BeforeEach
    void setUp() {
        requesterId = 1L;
        member = mock(Member.class);
        post = mock(Post.class);

        request = new CreateResumeRequest(
                "제목",
                List.of(10L),
                List.of(new CreateResumeRequest.QuestionDto(1L, "지원 동기를 서술하시오."))
        );
    }

    @Test
    @DisplayName("자소서 생성 성공")
    void createResume_success() {
        // given
        given(memberRepository.findById(requesterId))
                .willReturn(Optional.of(member));

        given(postRepository.findAllByIdWithCategories(request.postIds()))
                .willReturn(List.of(post));

        given(post.getContent())
                .willReturn("게시글 내용");

        given(promptTemplateLoader.load("resume.txt"))
                .willReturn(promptTemplate);

        given(promptTemplate.render(anyMap()))
                .willReturn("prompt");

        given(aiChatClient.chat(any()))
                .willReturn(new AiChatResponse("dummy"));

        given(parser.parse(anyString()))
                .willReturn(Map.of(
                        1L, new GeneratedAnswer("지원동기", "강조")
                ));

        given(resumeRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ResumeResponse response = resumeService.createResume(requesterId, request);

        // then
        assertThat(response.questions()).hasSize(1);
        assertThat(response.questions().get(0).answer()).isEqualTo("지원동기");
        assertThat(response.questions().get(0).description()).isEqualTo("강조");

        verify(aiChatClient).chat(any());
        verify(parser).parse(anyString());
        verify(resumeRepository).save(any());
    }

    @Test
    @DisplayName("AI 응답에 문항 답변이 누락되면 예외 발생")
    void createResume_answerMissing() {
        // given
        given(memberRepository.findById(requesterId))
                .willReturn(Optional.of(member));

        given(postRepository.findAllByIdWithCategories(request.postIds()))
                .willReturn(List.of(post));

        given(post.getContent())
                .willReturn("게시글 내용");

        given(promptTemplateLoader.load("resume.txt"))
                .willReturn(promptTemplate);

        given(promptTemplate.render(anyMap()))
                .willReturn("prompt");

        given(aiChatClient.chat(any()))
                .willReturn(new AiChatResponse("dummy"));

        given(parser.parse(anyString()))
                .willReturn(Map.of()); // order=1L 매핑이 통째로 빠짐

        // when & then
        assertThatThrownBy(() -> resumeService.createResume(requesterId, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getErrorCode())
                                .isEqualTo(ResumeErrorCode.AI_ANSWER_MISSING));
    }
}