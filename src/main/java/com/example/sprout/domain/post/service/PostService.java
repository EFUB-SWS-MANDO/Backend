package com.example.sprout.domain.post.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.exception.CategoryErrorCode;
import com.example.sprout.domain.category.repository.CategoryRepository;
import com.example.sprout.domain.comment.service.CommentService;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.dto.request.CreatePostRequest;
import com.example.sprout.domain.post.dto.response.PostDetailDto;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;

    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final PostCategoryService postCategoryService;

    @Transactional
    public PostDetailDto createPost(Long authorId, CreatePostRequest request) {
        Member author = getMemberById(authorId);

        //TODO: 카테고리 미설정 시, AI API 사용해서 카테고리 분류하도록 메소드 호출 (분기 처리 필요)
        if (request.categories().isEmpty()) {

        }
        //카테고리 검증
        List<String> types = request.categories().stream()
                .distinct()
                .toList();
        List<Category> categories = categoryRepository.findAllByTypeIn(types);
        if(categories.size() != types.size()) {
            log.warn("게시글 생성 - 존재하지 않는 카테고리 ");
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }
        //프로필 가져오기
        Profile authorProfile = getProfileByMember(author);

        //게시글 생성 및 저장
        Post newPost = request.toEntity(author);
        postRepository.save(newPost);
        categories.forEach(category -> postCategoryService.assignPostCategory(newPost, category));

        log.info("게시글 생성 성공 - postId: {}, authorId: {}, title: {}", newPost.getId(), authorId, newPost.getTitle());
        return PostDetailDto.of(newPost, authorProfile, true, false);
    }

    @Transactional
    public void deletePostByMember(Member member) {
        List<Post> postList = postRepository.findAllByAuthor(member);
        postList.forEach(this::deletePost);
    }

    //Post 단일 삭제
    private void deletePost(Post post) {
        //Post를 FK로 가지는 자식 엔티티 우선 삭제
        commentService.deleteByPost(post);
        postLikeService.deleteByPost(post);
        postCategoryService.deleteByPost(post);

        //Post 삭제
        postRepository.delete(post);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() ->{
                        log.warn("존재하지 않는 회원입니다.");
                        return new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
                });
    }

    private Profile getProfileByMember(Member member) {
        return profileRepository.findByMember(member)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 프로필입니다.");
                    return new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND);
                });
    }
}
