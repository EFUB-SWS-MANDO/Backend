package com.example.sprout.domain.template.entity;

import com.example.sprout.domain.template.enums.TemplateType;
import com.example.sprout.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "templates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Template extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TemplateType type;

    @Builder
    public Template(TemplateType type) {
        this.type = type;
    }

}
