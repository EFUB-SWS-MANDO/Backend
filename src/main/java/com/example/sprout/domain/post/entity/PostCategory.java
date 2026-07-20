package com.example.sprout.domain.post.entity;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.global.common.entity.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "post_categories",
    indexes = {
        @Index(name = "idx_post_category_post_id", columnList = "post_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "category_id"})
    }
)
@NoArgsConstructor(access =  AccessLevel.PROTECTED)
public class PostCategory extends BaseCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @Builder
    public PostCategory(Category category, Post post) {
        this.category = category;
        this.post = post;
    }
}
