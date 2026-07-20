package com.example.sprout.domain.template.cache;

import com.example.sprout.domain.template.enums.TemplateType;
import com.example.sprout.domain.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Profile("!test")
@Component
@RequiredArgsConstructor
@Slf4j
public class TemplateCacheWarmUp {

    private final TemplateService templateService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        for (TemplateType type : TemplateType.values()) {
            log.info("[Warm Up] Template 캐시 Loading : {}", type);
            templateService.getTemplate(type);
        }
    }

}
