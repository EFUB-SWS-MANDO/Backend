package com.example.sprout.domain.post.repository;

import com.example.sprout.domain.follow.entity.QFollow;
import com.example.sprout.domain.post.dto.request.PostCursor;
import com.example.sprout.domain.post.dto.request.PostSearchCondition;
import com.example.sprout.domain.post.entity.Post;
import com.example.sprout.domain.post.entity.QPost;
import com.example.sprout.domain.post.entity.QPostCategory;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QPost post = QPost.post;

    @Override
    public List<Post> search(PostSearchCondition condition, Long requesterId, PostCursor cursor) {
        return queryFactory
                .selectFrom(post)
                .where(
                        authorEq(condition.author()),
                        categoryIn(condition.category()),
                        followingFilter(condition.followingOnly(), requesterId),
                        keywordContains(condition.keyword()),
                        cursorPredicate(condition.sortBy(), condition.sortDirection(), cursor)
                )
                .orderBy(getOrder(condition.sortBy(), condition.sortDirection()))
                .limit(condition.limit() + 1)
                .fetch();
    }

    @Override
    public long count(PostSearchCondition condition, Long requesterId) {
        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        authorEq(condition.author()),
                        categoryIn(condition.category()),
                        followingFilter(condition.followingOnly(), requesterId),
                        keywordContains(condition.keyword())
                )
                .fetchOne();

        return total != null ? total : 0L;
    }

    //복합 커서 사전식 비교 (정렬값, id)
    private BooleanExpression cursorPredicate(String sortBy, String sortDirection, PostCursor cursor) {
        if (cursor == null) return null;
        boolean asc = "asc".equalsIgnoreCase(sortDirection);
        Long lastId = cursor.id();

        //좋아요 수에 따라 정렬할 때
        if ("likeCount".equals(sortBy)) {
            int lastLike = Integer.parseInt(cursor.sortValue());
            //이전 게시글의 좋아요 수가 더 클 때
            BooleanExpression primary = asc ?
                    post.likeCount.gt(lastLike) : post.likeCount.lt(lastLike);
            //이전 게시글의 좋아요 수와 동일 -> id로 tiebreaker
            BooleanExpression tie = post.likeCount.eq(lastLike)
                    .and(asc ? post.id.gt(lastId) : post.id.lt(lastId));
            return primary.or(tie);
        }

        //createdAt에 따라 정렬
        LocalDateTime lastCreated = LocalDateTime.parse(cursor.sortValue());
        BooleanExpression primary = asc ?
                post.createdAt.gt(lastCreated) : post.createdAt.lt(lastCreated);
        BooleanExpression tie = post.createdAt.eq(lastCreated)
                .and(asc ? post.id.gt(lastId) : post.id.lt(lastId));
        return primary.or(tie);

    }

    //정렬: 정렬키 + id 사용 tiebreaker (커서와 동일한 방향)
    private OrderSpecifier<?>[] getOrder(String sortBy, String sortDirection) {
        Order order = "asc".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

        OrderSpecifier<?> primary = "likeCount".equals(sortBy) ?
                new OrderSpecifier<>(order, post.likeCount) : new OrderSpecifier<>(order, post.createdAt);
        OrderSpecifier<Long> tieBreaker = new OrderSpecifier<>(order, post.id);

        return new OrderSpecifier[] {primary, tieBreaker};
    }


    private BooleanExpression categoryIn (List<String> categories) {
        if (categories == null || categories.isEmpty()) return null;
        QPostCategory postCategory = QPostCategory.postCategory;

        //서브쿼리: 아래 쿼리 실행해서 post id가 IN이면 true 반환
        return post.id.in(
                JPAExpressions.select(postCategory.post.id)
                        .from(postCategory)
                        .where(postCategory.category.type.in(categories))
        );
    }

    private BooleanExpression authorEq (Long authorId) {
        return (authorId == null) ? null : post.author.id.eq(authorId);
    }

    private BooleanExpression keywordContains (String keyword) {
        return (keyword == null || keyword.isBlank()) ?
                null : post.title.contains(keyword).or(post.content.containsIgnoreCase(keyword));
    }

    //팔로우 중인 작성자들의 게시글만 조회
    private BooleanExpression followingFilter (boolean followingOnly, Long requesterId) {
        if (!followingOnly) return null;
        QFollow follow = QFollow.follow;

        return post.author.id.in(
                JPAExpressions
                        .select(follow.followee.id)
                        .from(follow)
                        .where(follow.follower.id.eq(requesterId))
        );
    }
}
