package com.example.sprout.domain.template.service;

import com.example.sprout.domain.template.dto.TemplateDto;
import com.example.sprout.domain.template.entity.Template;
import com.example.sprout.domain.template.entity.TemplateValue;
import com.example.sprout.domain.template.enums.TemplateType;
import com.example.sprout.domain.template.exception.TemplateErrorCode;
import com.example.sprout.domain.template.repository.TemplateRepository;
import com.example.sprout.domain.template.repository.TemplateValueRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly=true)
@Slf4j
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateValueRepository  templateValueRepository;

    @Cacheable(
            key = "#type",
            value = "templates"
    )
    public TemplateDto getTemplate(TemplateType type) {
        log.info("[Cache Miss] 템플릿 DB 조회");

        Template template = getTemplateByType(type);

        List<String> values = templateValueRepository.findAllByTemplateId(template.getId())
                .stream()
                .map(TemplateValue::getValue)
                .toList();

        return TemplateDto.of(type, values);
    }

    // === Helper Method ===
    private Template getTemplateByType(TemplateType type) {
        return templateRepository.findByType(type)
                .orElseThrow(() -> new BusinessException(TemplateErrorCode.TEMPLATE_NOT_FOUND));
    }

}
