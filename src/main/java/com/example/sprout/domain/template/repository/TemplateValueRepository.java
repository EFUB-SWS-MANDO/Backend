package com.example.sprout.domain.template.repository;

import com.example.sprout.domain.template.entity.TemplateValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateValueRepository extends JpaRepository<TemplateValue, Long> {

    List<TemplateValue> findAllByTemplateId(Long templateId);

}
