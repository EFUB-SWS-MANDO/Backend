package com.example.sprout.domain.template.entity;

import com.example.sprout.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "template_values")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateValue extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;

    @Column(name = "value", nullable = false)
    private String value;

    @Builder
    public TemplateValue(Template template, String value) {
        this.template = template;
        this.value = value;
    }

}
