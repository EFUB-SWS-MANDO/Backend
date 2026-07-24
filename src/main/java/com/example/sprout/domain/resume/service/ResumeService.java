package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostCategory;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.post.repository.PostCategoryRepository;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.resume.dto.ai.GeneratedAnswer;
import com.example.sprout.domain.resume.dto.request.CreateResumeRequest;
import com.example.sprout.domain.resume.dto.request.GetResumeListCondition;
import com.example.sprout.domain.resume.dto.response.GetResumeListResponse;
import com.example.sprout.domain.resume.dto.response.ResumeDetailItem;
import com.example.sprout.domain.resume.dto.response.ResumeListItem;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.sprout.global.common.util.CursorPageUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final ResumeRepository resumeRepository;

    private final ResumeDraftService resumeDraftService;

    private final AiChatClient aiChatClient;
    private final PromptTemplateLoader promptTemplateLoader;
    private final ResumeAiResponseParser parser;

    // 자소서 생성
    @Transactional
    public ResumeResponse createResume(Long requesterId, CreateResumeRequest request) {
        Member requester = getMember(requesterId);
        List<Post> posts = getPostList(request.postIds(), requesterId);
        List<PostCategory> postCategoryList = getPostCategoryList(request.postIds());

        // postId 기준 Post, PostCategory 그룹핑
        Map<Long, List<PostCategory>> categoriesByPostId = groupCategoriesByPostId(postCategoryList);

        // Post -> 하나의 문자열로 합침
        String postSummary = buildPostSummary(posts, categoriesByPostId);
        Resume resume = request.toEntity(requester);

        Map<Long, GeneratedAnswer> answerMap = generateAllAnswers(postSummary, request.questions());

        List<ResumeDraft> drafts = request.questions().stream()
                .map(q -> toResumeDraft(resume, q, getGeneratedAnswer(answerMap, q.order())))
                .toList();

        resume.getResumeDraftList().addAll(drafts);

        Resume saved = resumeRepository.save(resume);

        return toResponse(saved);
    }

    // 자소서 목록 조회
    @Transactional(readOnly = true)
    public GetResumeListResponse getResumeList(Long requesterId, GetResumeListCondition condition) {
        int limit = condition.limit();
        Long idAfter = condition.idAfter();
        String keyword = condition.keyword();

        Member author = getMember(requesterId);

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Resume> resumeList = resumeRepository.findPageByAuthorAndKeyword(author, idAfter, keyword, pageable);

        boolean hasNext = hasNextPage(resumeList, limit);

        List<Resume> pageResumeList = trimToPageSize(resumeList, limit, hasNext);
        List<ResumeListItem> pageResumeListItem = toResumeListItem(pageResumeList);

        Long nextIdAfter = resolveNextIdAfter(pageResumeList, hasNext, Resume::getId);
        Long totalElements = resumeRepository.countAllByAuthorAndKeyword(author, keyword);

        return GetResumeListResponse.of(pageResumeListItem, nextIdAfter, hasNext, totalElements);
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
    private List<Post> getPostList(List<Long> postIds, Long requesterId) {
        List<Post> postList = postRepository.findAllById(postIds);

        if (postIds.size() != postList.size()) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }

        // 본인의 게시글을 가져왔는지 확인
        boolean hasUnauthorizedPost = postList.stream()
                .anyMatch(post -> !post.getAuthor().getId().equals(requesterId));

        if (hasUnauthorizedPost) {
            throw new BusinessException(PostErrorCode.POST_ACCESS_DENIED);
        }

        return postList;
    }

    // 게시글 카테고리 조회
    private List<PostCategory> getPostCategoryList(List<Long> postIds) {
        return postCategoryRepository.findAllByPostIdIn(postIds);
    }

    // postId 기준 카테고리 그룹핑
    private Map<Long, List<PostCategory>> groupCategoriesByPostId(List<PostCategory> postCategoryList) {
        return postCategoryList.stream()
                .collect(Collectors.groupingBy(pc -> pc.getPost().getId()));
    }

    // Post -> 하나의 문자열로 합침
    private String buildPostSummary(List<Post> postList, Map<Long, List<PostCategory>> categoriesByPostId) {
        return postList.stream()
                .map(post -> {
                    List<String> categoryNames = categoriesByPostId
                            .getOrDefault(post.getId(), List.of())
                            .stream()
                            .map(pc -> pc.getCategory().getType())
                            .toList();

                    String prefix = categoryNames.isEmpty()
                            ? ""
                            : "[" + String.join(", ", categoryNames) + "]";

                    return prefix + post.getContent();
                })
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
                .temperature(1.0)
                .maxTokens(Math.min(2500 * questions.size(), 8000))
                .build();

        AiChatResponse response = aiChatClient.chat(aiChatRequest);
        return parser.parse(response.content());
    }

    private GeneratedAnswer getGeneratedAnswer(Map<Long, GeneratedAnswer> answerMap, Long order) {
        GeneratedAnswer generated = answerMap.get(order);
        if (generated == null) {
            log.error("AI 응답에 order={} 문항 답변 누락", order);
            throw new BusinessException(ResumeErrorCode.AI_ANSWER_MISSING);
        }
        return generated;
    }

    private ResumeDraft toResumeDraft(Resume resume, CreateResumeRequest.QuestionDto q, GeneratedAnswer generated) {
        return ResumeDraft.builder()
                .resume(resume)
                .orderIndex(q.order())
                .question(q.content())
                .answer(generated.answer())
                .description(generated.description())
                .build();
    }

    // 응답 형식 변경
    private ResumeResponse toResponse(Resume resume) {
        List<ResumeDetailItem> items = resume.getResumeDraftList().stream()
                .map(ResumeDetailItem::from).toList();

        return ResumeResponse.of(resume, items);
    }

    private List<ResumeListItem> toResumeListItem(List<Resume> resumes) {
        return resumes.stream()
                .map(ResumeListItem::from).toList();
    }
}
