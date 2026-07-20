package com.example.sprout.domain.template.cache;

import com.example.sprout.domain.template.enums.TemplateType;
import com.example.sprout.domain.template.service.TemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TemplateCacheWarmUpTest {

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private TemplateCacheWarmUp templateCacheWarmUp;

    @Test
    @DisplayName("ApplicationReadyEvent 발생 시 템플릿 목록을 조회하여 캐시 웜업")
    void warmUp() {
        // when
        templateCacheWarmUp.warmUp();

        // then
        verify(templateService, times(1)).getTemplate(any(TemplateType.class));
    }
}
