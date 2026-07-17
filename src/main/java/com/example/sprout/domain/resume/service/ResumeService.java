package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.resume.dto.ai.GeneratedAnswer;
import com.example.sprout.domain.resume.dto.request.CreateResumeRequest;
import com.example.sprout.domain.resume.dto.response.ResumeDetailItem;
import com.example.sprout.domain.resume.dto.response.ResumeResponse;
import com.example.sprout.domain.resume.entity.Resume;
import com.example.sprout.domain.resume.entity.ResumeDraft;
import com.example.sprout.domain.resume.exception.ResumeErrorCode;
import com.example.sprout.domain.resume.parser.ResumeAiResponseParser;
import com.example.sprout.domain.resume.repository.ResumeRepository;
import com.example.sprout.global.ai.client.AiChatClient;
import com.example.sprout.global.ai.dto.AiChatRequest;
import com.example.sprout.global.ai.dto.AiChatResponse;
import com.example.sprout.global.ai.dto.AiMessage;
import com.example.sprout.global.ai.prompt.PromptTemplateLoader;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final ResumeRepository resumeRepository;

    private final ResumeDraftService resumeDraftService;

    private final AiChatClient aiChatClient;
    private final PromptTemplateLoader promptTemplateLoader;
    private final ResumeAiResponseParser parser;

    // 자소서 생성
    @Transactional
    public ResumeResponse createResume(Long requesterId, CreateResumeRequest request) {
        Member requester = getMember(requesterId);
        List<Post> posts = getPostList(request.postIds());

        // Post -> 하나의 문자열로 합침
        String postSummary = buildPostSummary(posts);
        Resume resume = request.toEntity(requester);

        Map<Long, GeneratedAnswer> answerMap = generateAllAnswers(postSummary, request.questions());

        for (CreateResumeRequest.QuestionDto q: request.questions()) {
            GeneratedAnswer generated = answerMap.get(q.order());
            if (generated == null) {
                log.error("AI 응답에 order={} 문항 답변 누락", q.order());
                throw new BusinessException(ResumeErrorCode.AI_ANSWER_MISSING);
            }

            ResumeDraft draft = ResumeDraft.builder()
                    .resume(resume)
                    .orderIndex((q.order()))
                    .question(q.content())
                    .answer(generated.answer())
                    .description(generated.description())
                    .build();

            resume.getResumeDraftList().add(draft);
        }

        Resume saved = resumeRepository.save(resume);

        return toResponse(saved);
    }

    //회원 탈퇴 시 resume 및 resumeDraft 삭제
    @Transactional
    public void deleteByMember(Member member) {
        //resumeDraft 삭제
        List<Resume> resumeList = resumeRepository.findAllByAuthor(member);
        resumeList.forEach(resumeDraftService::deleteAllByResume);

        //resume 삭제
        resumeRepository.deleteAllByAuthor(member);
    }


    // Helper 함수

    // Member 조회
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("존재하지 않는 회원 - memberId: {}", memberId);
                    return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
    }

    // 게시글 리스트 조회
    private List<Post> getPostList(List<Long> postIds) {
        List<Post> postList = postRepository.findAllByIdWithCategories(postIds);

        if (postIds.size() != postList.size()) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        return postList;
    }

    // Post -> 하나의 문자열로 합침
    private String buildPostSummary(List<Post> postList) {
        return postList.stream()
                .map(Post::getContent)
                .collect(Collectors.joining("\n\n"));
    }

    private String buildQuestionListText(List<CreateResumeRequest.QuestionDto> questions) {
        return questions.stream()
                .map(q -> q.order() + ". " + q.content())
                .collect(Collectors.joining("\n"));
    }

    private Map<Long, GeneratedAnswer> generateAllAnswers(String postSummary, List<CreateResumeRequest.QuestionDto> questions) {
        var template = promptTemplateLoader.load("resume.txt");
        String systemPrompt = template.render(Map.of(
                "posts", postSummary,
                "questions", buildQuestionListText(questions)
        ));

        AiChatRequest aiChatRequest = AiChatRequest.builder()
                .messages(List.of(new AiMessage("system", systemPrompt)))
                .temperature(0.8)
                .maxTokens(Math.min(500 * questions.size(), 1500))
                .build();

        AiChatResponse response = aiChatClient.chat(aiChatRequest);
        return parser.parse(response.content());
    }

    // 응답 형식 변경
    private ResumeResponse toResponse(Resume resume) {
        List<ResumeDetailItem> items = resume.getResumeDraftList().stream()
                .map(d -> new ResumeDetailItem(
                        d.getId(),
                        d.getOrderIndex(),
                        d.getQuestion(),
                        d.getAnswer(),
                        d.getDescription()
                ))
                .toList();

        return new ResumeResponse(resume.getId(), resume.getTitle(), resume.getCreatedAt(), items);
    }

}
