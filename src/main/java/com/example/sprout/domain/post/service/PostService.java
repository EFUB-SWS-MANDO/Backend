package com.example.sprout.domain.post.service;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.exception.CategoryErrorCode;
import com.example.sprout.domain.category.repository.CategoryRepository;
import com.example.sprout.domain.comment.repository.CommentRepository;
import com.example.sprout.domain.comment.service.CommentService;
import com.example.sprout.domain.file.dto.response.MovedFileInfo;
import com.example.sprout.domain.file.service.S3FileService;
import com.example.sprout.domain.file.service.S3PresignedUrlService;
import com.example.sprout.domain.follow.repository.FollowRepository;
import com.example.sprout.domain.member.entity.Member;
import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.domain.post.dto.request.PostCursor;
import com.example.sprout.domain.post.dto.request.CreatePostRequest;
import com.example.sprout.domain.post.dto.request.PostSearchCondition;
import com.example.sprout.domain.post.dto.request.UpdatePostRequest;
import com.example.sprout.domain.post.dto.response.AuthorDto;
import com.example.sprout.domain.post.dto.response.PostDetailResponse;
import com.example.sprout.domain.post.dto.response.PostListResponse;
import com.example.sprout.domain.post.dto.response.PostSummaryDto;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.PostCategory;
import com.example.sprout.domain.post.entity.PostFile;
import com.example.sprout.domain.post.exception.PostErrorCode;
import com.example.sprout.domain.post.repository.PostCategoryRepository;
import com.example.sprout.domain.post.repository.PostLikeRepository;
import com.example.sprout.domain.post.repository.PostRepository;
import com.example.sprout.domain.post.repository.projection.PostCategoryView;
import com.example.sprout.domain.post.repository.projection.PostCommentCount;
import com.example.sprout.domain.profile.entity.Profile;
import com.example.sprout.domain.profile.exception.ProfileErrorCode;
import com.example.sprout.domain.profile.repository.ProfileRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCategoryRepository postCategoryRepository;

    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final PostCategoryService postCategoryService;
    private final S3FileService s3FileService;
    private final PostFileService postFileService;
    private final S3PresignedUrlService s3PresignedUrlService;

    private record SummaryLookup(
            Map<Long, Profile> profileMap,
            Set<Long> followingIds,
            Map<Long, Long> commentCountMap,
            Set<Long> likedPostIds,
            Map<Long, List<String>> categoriesMap
    ) {}

    //게시글 생성
    @Transactional
    public PostDetailResponse createPost(Long authorId, CreatePostRequest request) {
        Member author = getMemberById(authorId);

        //카테고리 검증
        List<Category> categories = resolveCategories(request.categories());

        //게시글 생성 및 저장
        Post newPost = request.toEntity(author);
        postRepository.save(newPost);
        postCategoryService.assignPostCategories(newPost, categories);

        //S3 이미지 처리
        resolveFiles(newPost, request.fileKeys());

        log.info("게시글 생성 성공 - postId: {}, authorId: {}, title: {}", newPost.getId(), authorId, newPost.getTitle());
        return toPostDetailResponse(author, newPost);
    }

    //게시글 목록 조회
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
    public PostDetailResponse getPostDetail(Long requesterId, Long postId) {
        Member requester = getMemberById(requesterId);
        Post post = getPostById(postId);

        //비공개 글 조회 권한 검증
        validatePrivatePostAuthorization(requester, post);

        log.info("게시글 상세조회 성공 -postId: {}, requesterId: {}, authorId: {}, title: {}",
                post.getId(), requesterId, post.getAuthor().getId(), post.getTitle());
        return toPostDetailResponse(requester, post);
    }

    //게시글 수정
    @Transactional
    public PostDetailResponse updatePost(Long requesterId, Long postId, UpdatePostRequest request) {
        Member requester = getMemberById(requesterId);
        Post post = getPostById(postId);
        Member author = post.getAuthor();

        //수정 권한 확인
        if (!validateAuthor(requester, author)) {
            log.warn("게시글 수정 권한 없음 - requesterId: {}, authorId: {}", requesterId, author.getId());
            throw new BusinessException(PostErrorCode.POST_ACCESS_DENIED);
        }

        List<Category> newCategories = resolveCategories(request.categories());

        //게시글 수정 - 카테고리 수정 - 이미지 저장 수정
        post.updatePost(request.title(), request.content(), request.isPrivate());
        postCategoryService.updatePostCategories(post, newCategories);
        postFileService.updatePostFile(post, request.fileKeys());

        log.info("게시글 수정 성공 - requesterId: {}, postId: {}, authorId: {}", requesterId, postId, author.getId());
        return toPostDetailResponse(requester, post);
    }

    @Transactional
    public void deletePost(Long requesterId, Long postId) {
        Member requester = getMemberById(requesterId);
        Post post = getPostById(postId);

        if (!validateAuthor(requester, post.getAuthor())) {
            log.warn("게시글 삭제 권한 없음 - requesterId: {}, authorId: {}", requesterId, post.getAuthor().getId());
            throw new BusinessException(PostErrorCode.POST_ACCESS_DENIED);
        }

        deletePost(post);
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

        //이미지 삭제
        postFileService.deleteByPost(post);

        //Post 삭제
        postRepository.delete(post);
    }

    //카테고리 존재 여부 검증 및 카테고리 할당
    private List<Category> resolveCategories (List<String> rawTypes) {

        //TODO: 카테고리 미설정 시, AI API 사용해서 카테고리 분류하도록 메소드 호출 (분기 처리 필요)
        if (rawTypes.isEmpty()) {

        }

        List<String> types = rawTypes.stream().distinct().toList();
        List<Category> categories = categoryRepository.findAllByTypeIn(types);

        if (categories.size() != types.size()) {
            log.warn("존재하지 않는 카테고리입니다.");
            throw new BusinessException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }

        return categories;
    }

    //이미지 및 파일 S3 저장 메소드
    private void resolveFiles(Post post, List<String> fileKeys) {
        if(fileKeys == null || fileKeys.isEmpty()) return;

        //파일 영구저장 key 및 정보 가져오기
        List<MovedFileInfo> fileInfos =  s3FileService.moveToPermanent(fileKeys, post.getId());
        postFileService.createPostFile(post, fileInfos);
    }

    //권한 검증
    private boolean validateAuthor(Member requester, Member author) {
        return requester.getId().equals(author.getId());
    }

    private void validatePrivatePostAuthorization(Member requester, Post post) {
        if (post.isPrivate() && !validateAuthor(requester, post.getAuthor())) {
            log.warn("비공개 게시글 작성자 외 조회 요청 - requesterId: {}, postId: {}, authorId: {}", requester.getId(), post.getId(), post.getAuthor().getId());
            throw new BusinessException(PostErrorCode.POST_ACCESS_DENIED);
        }
    }

    private AuthorDto toAuthorDto (Profile authorProfile, boolean isFollowing) {
        String profileImage = s3PresignedUrlService.createDownloadUrlOrNull(authorProfile.getProfileImage());

        return AuthorDto.of(authorProfile, profileImage, isFollowing);
    }

    private PostSummaryDto toPostSummary(Post post, Long requesterId, SummaryLookup lookup) {
        Long postId = post.getId();
        Long authorId = post.getAuthor().getId();

        //배치 조회값 매핑
        //1-AuthorDto 구성
        boolean isMine = requesterId.equals(authorId);
        boolean isFollowing = !isMine && lookup.followingIds().contains(authorId);
        Profile authorProfile = lookup.profileMap().get(authorId);
        if (authorProfile == null) {
            log.warn("게시글 작성자 프로필이 존재하지 않음 - postId: {}, authorId: {}", postId, authorId);
            throw new BusinessException(ProfileErrorCode.PROFILE_NOT_FOUND);
        }
        AuthorDto author = toAuthorDto(authorProfile, isFollowing);

        //2-카테고리 구성
        List<String> categories = lookup.categoriesMap().getOrDefault(postId, List.of());

        //3-좋아요 수, 댓글 수 구성
        boolean isLike = lookup.likedPostIds().contains(postId);
        long commentCount = lookup.commentCountMap().getOrDefault(postId, 0L);

        return PostSummaryDto.of(post, author, categories, commentCount, isLike);
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

    private PostDetailResponse toPostDetailResponse(Member requester, Post post) {
        Member author = post.getAuthor();
        Profile authorProfile = getProfileByMember(author);

        List<String> fileKeys = getPostFileKeys(post);
        List<String> fileUrls = s3PresignedUrlService.createDownloadUrls(fileKeys);

        List<String> categories = getPostCategories(post);

        boolean isMine = requester.getId().equals(author.getId());
        boolean isFollowing = !isMine && getIsFollowing(requester, author);
        boolean isLike = getIsLike(requester, post);

        AuthorDto authorDto = toAuthorDto(authorProfile, isFollowing);

        return PostDetailResponse.of(post, authorDto, fileUrls, categories, isMine, isLike);
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

    private boolean getIsLike(Member member, Post post) {
        return postLikeRepository.existsByMemberAndPost(member, post);
    }

    private List<String> getPostCategories(Post post) {
        List<Category> categories = postCategoryRepository.findAllByPost(post).stream()
                .map(PostCategory::getCategory)
                .toList();

        return categories.stream()
                .map(Category::getType)
                .toList();
    }

    private List<String> getPostFileKeys(Post post) {
        List<PostFile> postFiles = postFileService.getPostFiles(post);

        return postFiles.stream()
                .map(PostFile::getS3Key)
                .toList();
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
}
