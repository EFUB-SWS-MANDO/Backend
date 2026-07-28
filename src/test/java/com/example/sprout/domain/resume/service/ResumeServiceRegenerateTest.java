package com.example.sprout.domain.resume.service;


import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostCategory;
import com.example.sprout.domain.post.repository.PostCategoryRepository;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.resume.dto.ai.GeneratedAnswer;
import com.example.sprout.domain.resume.dto.request.CreateResumeRequest;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.entity.ResumeDraft;
import com.example.sprout.domain.resume.entity.ResumeSourcePost;
import com.example.sprout.domain.resume.exception.ResumeErrorCode;
import com.example.sprout.domain.resume.parser.ResumeAiResponseParser;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import com.example.sprout.domain.resume.repository.ResumeSourcePostRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResumeServiceRegenerateTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCategoryRepository postCategoryRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeSourcePostRepository resumeSourcePostRepository;

    @Mock
    private AiChatClient aiChatClient;

    @Mock
    private PromptTemplateLoader promptTemplateLoader;

    @Mock
    private PromptTemplateLoader.PromptTemplate promptTemplate;

    @Mock
    private ResumeAiResponseParser parser;

    @InjectMocks
    private ResumeService resumeService;

    private Long requesterId;
    private Long resumeId;
    private Member member;
    private Post post;
    private PostCategory postCategory;
    private Category category;
    private CreateResumeRequest request;

    @BeforeEach
    void setUp() {
        requesterId = 1L;
        member = mock(Member.class);
        post = mock(Post.class);
        category = mock(Category.class);
        postCategory = mock(PostCategory.class);
        resumeId = 100L;

        request = new CreateResumeRequest(
                "제목",
                List.of(10L),
                List.of(new CreateResumeRequest.QuestionDto(1L, "지원 동기를 서술하시오."))
        );
    }

    @Test
    @DisplayName("자소서 재생성 시 기존 draft의 answer와 description이 갱신된다")
    void regenerate_resume_success() {

        Member author = mock(Member.class);
        given(author.getId()).willReturn(requesterId);

        ResumeDraft draft = ResumeDraft.builder()
                .orderIndex(1L)
                .question("지원 동기를 서술하시오.")
                .answer("기존 답변")
                .description("기존 설명")
                .build();

        Resume resume = mock(Resume.class);
        given(resume.getAuthor()).willReturn(author);
        given(resume.getResumeDraftList()).willReturn(List.of(draft));

        Post post = mock(Post.class);
        ResumeSourcePost sourcePost = ResumeSourcePost.builder().post(post).resume(resume).build();

        given(resumeRepository.findById(resumeId)).willReturn(Optional.of(resume));
        given(resumeSourcePostRepository.findAllByResumeId(resumeId)).willReturn(List.of(sourcePost));
        given(postCategoryRepository.findAllByPostIdIn(any())).willReturn(List.of());

        given(promptTemplateLoader.load("resume.txt")).willReturn(promptTemplate);
        given(promptTemplate.render(anyMap())).willReturn("prompt");
        given(aiChatClient.chat(any())).willReturn(new AiChatResponse("{...}"));
        given(parser.parse(anyString())).willReturn(Map.of(1L, new GeneratedAnswer("새 답변", "새 설명")));

        resumeService.regenerateResume(requesterId, resumeId);

        assertThat(draft.getAnswer()).isEqualTo("새 답변");
        assertThat(draft.getDescription()).isEqualTo("새 설명");
    }

    @Test
    @DisplayName("자소서 재생성 시 작성자가 아니면 예외가 발생한다")
    void regenerate_resume_failed_author_forbidden() {

        Long otherAuthorId = 999L;
        Member otherAuthor = mock(Member.class);
        given(otherAuthor.getId()).willReturn(otherAuthorId);

        Resume resume = mock(Resume.class);
        given(resume.getAuthor()).willReturn(otherAuthor);
        given(resumeRepository.findById(resumeId)).willReturn(Optional.of(resume));

        assertThatThrownBy(() -> resumeService.regenerateResume(requesterId, resumeId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ResumeErrorCode.RESUME_ACCESS_DENIED);
    }
}
