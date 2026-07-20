package com.example.sprout.domain.template.repository;

import com.example.sprout.domain.template.entity.Template;
import com.example.sprout.domain.template.enums.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByType(TemplateType type);
}
