package com.example.sprout.domain.motivation.entity;

import com.example.sprout.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "motivations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Motivation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "content", nullable = false, unique = true)
    private String content;

    @Column(name = "display_order", nullable = false, unique = true)
    private int displayOrder;

    @Builder
    public Motivation(String content, int displayOrder) {
        this.content = content;
        this.displayOrder = displayOrder;
    }
}
