package com.example.sprout.domain.template.dto;

import com.example.sprout.domain.template.enums.TemplateType;

import java.util.List;

public record TemplateDto(
        TemplateType type,
        List<String> values
) {
    public static TemplateDto of(TemplateType templateType, List<String> values) {
        return new TemplateDto(templateType, values);
    }
}
