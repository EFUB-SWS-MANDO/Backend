package com.example.sprout.domain.post.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.exception.CategoryErrorCode;
import com.example.sprout.domain.category.repository.CategoryRepository;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.comment.service.CommentService;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.dto.request.PostCursor;
import com.example.sprout.domain.post.dto.request.CreatePostRequest;
import com.example.sprout.domain.post.dto.request.PostSearchCondition;
import com.example.sprout.domain.post.dto.request.UpdatePostRequest;
import com.example.sprout.domain.post.dto.response.AuthorDto;
import com.example.sprout.domain.post.dto.response.PostDetailDto;
import com.example.sprout.domain.post.dto.response.PostListResponse;
import com.example.sprout.domain.post.dto.response.PostSummaryDto;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.post.repository.PostCategoryRepository;
import com.example.sprout.domain.post.repository.PostLikeRepository;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.post.summary.PostCategoryView;
import com.example.sprout.domain.post.summary.PostCommentCount;
import com.example.sprout.domain.post.summary.SummaryLookup;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.sprout.global.common.util.CursorPageUtils.hasNextPage;
import static com.example.sprout.global.common.util.CursorPageUtils.trimToPageSize;

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
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCategoryRepository postCategoryRepository;

    //게시글 생성
    @Transactional
    @CacheEvict(value = "postsFirstPage", allEntries = true)
    public PostDetailDto createPost(Long authorId, CreatePostRequest request) {
        Member author = getMemberById(authorId);

        //TODO: 카테고리 미설정 시, AI API 사용해서 카테고리 분류하도록 메소드 호출 (분기 처리 필요)
        if (request.categories().isEmpty()) {

        }
        //카테고리 검증
        List<Category> categories = resolveCategories(request.categories());

        //프로필 가져오기
        Profile authorProfile = getProfileByMember(author);

        //게시글 생성 및 저장
        Post newPost = request.toEntity(author);
        postRepository.save(newPost);
        postCategoryService.assignPostCategories(newPost, categories);

        log.info("게시글 생성 성공 - postId: {}, authorId: {}, title: {}", newPost.getId(), authorId, newPost.getTitle());
        return PostDetailDto.of(newPost, authorProfile, true, false);
    }

    //게시글 목록 조회
    @Cacheable(
            value = "postsFirstPage",
            key = "#memberId + ':' + #condition.sortBy() + ':' + #condition.sortDirection()",
            condition = "#condition.cursor() == null " +
                    "and #condition.category() == null " +
                    "and #condition.author() == null " +
                    "and #condition.keyword() == null " +
                    "and !#condition.followingOnly()"
    )
    @Transactional(readOnly = true)
    public PostListResponse getPostList(Long memberId, PostSearchCondition condition) {
        Member requester = getMemberById(memberId);
        PostCursor cursor = PostCursor.decode(condition.cursor()); //요청 커서 디코딩

        //게시글 가져오기 (limit+1개)
        List<Post> postList = postRepository.search(condition, memberId, cursor);
        long totalElements = postRepository.count(condition, memberId);

        //DTO 응답준비
        boolean hasNext = hasNextPage(postList, condition.limit());
        postList = trimToPageSize(postList, condition.limit(), hasNext);

        String nextCursor = hasNext ?
                PostCursor.of(postList.get(postList.size()-1), condition.sortBy()).encode() : null;

        //PostSummaryDto 리스트로 변환
        List<PostSummaryDto> postSummaryList = toSummaryList(postList, memberId);
        return PostListResponse.of(
                postSummaryList,
                nextCursor, hasNext,
                condition.sortBy(), condition.sortDirection(), totalElements);
    }

    //게시글 상세조회
    @Transactional(readOnly = true)
    public PostDetailDto getPostDetail(Long requesterId, Long postId) {
        Member requester = getMemberById(requesterId);
        Post post = getPostById(postId);
        Member author = post.getAuthor();
        Profile authorProfile = getProfileByMember(author);

        boolean isMine = requester.getId().equals(author.getId());
        boolean isFollowing = !isMine && getIsFollowing(requester, author);

        log.info("게시글 상세조회 성공 -postId: {}, requesterId: {}, authorId: {}, title: {}",
                post.getId(), requesterId, author.getId(), post.getTitle());
        return PostDetailDto.of(post, authorProfile, isMine, isFollowing);
    }

    //게시글 수정
    @CacheEvict(value = "postsFirstPage", allEntries = true)
    @Transactional
    public PostDetailDto updatePost(Long requesterId, Long postId, UpdatePostRequest request) {
        Member requester = getMemberById(requesterId);

        Post post = getPostById(postId);
        Member author = post.getAuthor();

        //수정 권한 확인
        if (!isAuthor(requester, author)) {
            log.warn("게시글 수정 권한 없음 - requesterId: {}, authorId: {}", requesterId, author.getId());
            throw new BusinessException(PostErrorCode.POST_ACCESS_DENIED);
        }
        Profile authorProfile = getProfileByMember(author);

        //TODO: 카테고리 미설정 시, AI API 사용해서 카테고리 분류하도록 메소드 호출 (분기 처리 필요)
        if (request.categories().isEmpty()) {

        }

        List<Category> newCategories = resolveCategories(request.categories());

        //게시글 수정
        post.updatePost(request.title(), request.content());
        postCategoryService.updatePostCategories(post, newCategories);

        log.info("게시글 수정 성공 - requesterId: {}, postId: {}, authorId: {}", requesterId, postId, author.getId());
        return PostDetailDto.of(post, authorProfile, true, false);
    }

    @CacheEvict(value = "postsFirstPage", allEntries = true)
    @Transactional
    public void deletePostWithChildren(Long requesterId, Long postId) {
        Member requester = getMemberById(requesterId);
        Post post = getPostById(postId);

        if (!isAuthor(requester, post.getAuthor())) {
            log.warn("게시글 삭제 권한 없음 - requesterId: {}, authorId: {}", requesterId, post.getAuthor().getId());
            throw new BusinessException(PostErrorCode.POST_ACCESS_DENIED);
        }

        deletePostWithChildren(post);
    }

    @CacheEvict(value = "postsFirstPage", allEntries = true)
    @Transactional
    public void deletePostByMember(Member member) {
        List<Post> postList = postRepository.findAllByAuthor(member);
        postList.forEach(this::deletePostWithChildren);
    }

    //Post 단일 삭제
    private void deletePostWithChildren (Post post) {
        //Post를 FK로 가지는 자식 엔티티 우선 삭제
        commentService.deleteByPost(post);
        postLikeService.deleteByPost(post);
        postCategoryService.deleteByPost(post);

        //Post 삭제
        postRepository.delete(post);
    }

    //private 헬퍼 메소드
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

    private Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("게시글이 존재하지 않습니다.");
                    return new BusinessException(PostErrorCode.POST_NOT_FOUND);
                });
    }

    private boolean getIsFollowing(Member follower, Member followee) {
        return followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId());
    }

    private List<Category> resolveCategories (List<String> rawTypes) {
        List<String> types = rawTypes.stream().distinct().toList();
        List<Category> categories = categoryRepository.findAllByTypeIn(types);

        if (categories.size() != types.size()) {
            log.warn("존재하지 않는 카테고리입니다.");
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }

        return categories;
    }

    private boolean isAuthor(Member requester, Member author) {
        return requester.getId().equals(author.getId());
    }

    private PostSummaryDto toPostSummary(Post post, Long requesterId, SummaryLookup lookup) {
        Long postId = post.getId();
        Long authorId = post.getAuthor().getId();

        //배치 조회값 매핑
        //1-AuthorDto 구성
        boolean isMine = requesterId.equals(authorId);
        boolean isFollowing = !isMine && lookup.followingIds().contains(authorId);
        AuthorDto author = AuthorDto.of(lookup.profileMap().get(authorId), isFollowing);
        //2-카테고리 구성
        List<String> categories = lookup.categoriesMap().getOrDefault(postId, List.of());
        //3-좋아요 수, 댓글 수 구성
        boolean isLike = lookup.likedPostIds().contains(postId);
        long commentCount = lookup.commentCountMap().getOrDefault(postId, 0L);

        return PostSummaryDto.of(post, author, categories, commentCount, isLike);
    }

    //배치조회 - 게시글 목록에 필요한 요소들 미리 배치 조회 (N+1 방지)
    private SummaryLookup getSummaryLookup(List<Post> posts, Long requesterId) {

        //키 수집 (N+1 방지)
        List<Long> postIds = posts.stream()
                .map(Post::getId).toList();
        List<Long> authorIds = posts.stream()
                .map(post -> post.getAuthor().getId())
                .distinct().toList();

        //작성자 프로필 전체 조회
        Map<Long, Profile> profileMap = profileRepository.findByMemberIdIn(authorIds).stream()
                .collect(Collectors.toMap(profile -> profile.getMember().getId(), Function.identity()));
        //팔로워 ID 전체 조회
        Set<Long> followingIds = new HashSet<>(
                followRepository.findFolloweeIdsByFollowerIdAndFolloweeIdIn(requesterId, authorIds)
        );
        //댓글 수 전체 조회
        Map<Long, Long> commentCountMap = commentRepository.countByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(PostCommentCount::getPostId, PostCommentCount::getCount));
        //좋아요 누른 게시글 id 목록 조회
        Set<Long> likedPostIds = new HashSet<> (
                postLikeRepository.findLikedPostIdsByMemberIdAndPostIdIn(requesterId, postIds)
        );
        //게시글별 카테고리 조회
        Map<Long, List<String>> categoriesMap = postCategoryRepository.findCategoryTypesByPostIdIn(postIds).stream()
                .collect(Collectors.groupingBy(PostCategoryView::getPostId,
                        Collectors.mapping(PostCategoryView::getType, Collectors.toList())));

        //배치 lookup 레코드 생성 및 반환
        return new SummaryLookup(profileMap, followingIds, commentCountMap, likedPostIds, categoriesMap);
    }

    private List<PostSummaryDto> toSummaryList(List<Post> posts, Long requesterId) {
        if (posts.isEmpty()) return List.of();

        //배치 조회 결과물
        SummaryLookup lookup = getSummaryLookup(posts, requesterId);

        //PostSummaryDto 리스트로 매핑해 반환
        return posts.stream()
                .map(post -> toPostSummary(post, requesterId, lookup))
                .toList();
    }
}
