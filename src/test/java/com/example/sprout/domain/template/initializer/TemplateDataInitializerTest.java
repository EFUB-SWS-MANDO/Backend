package com.example.sprout.domain.template.initializer;

import com.example.sprout.domain.template.entity.Template;
import com.example.sprout.domain.template.repository.TemplateRepository;
import com.example.sprout.domain.template.repository.TemplateValueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TemplateDataInitializerTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateValueRepository templateValueRepository;

    @InjectMocks
    private TemplateDataInitializer templateDataInitializer;

    @Test
    @DisplayName("DB에 템플릿 데이터가 0개면 초기 데이터 저장")
    void run_whenDBIsEmpty_SavesInitialTemplates() throws Exception {
        // given
        given(templateRepository.count()).willReturn(0L);

        // when
        templateDataInitializer.run();

        // then
        verify(templateRepository).save(any(Template.class));
        verify(templateValueRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("DB에 템플릿 데이터가 존재하면 초기 데이터 저장 없이 스킵")
    void run_whenDBIsNotEmpty_SkipsSaving() throws Exception {
        // given
        given(templateRepository.count()).willReturn(1L);

        // when
        templateDataInitializer.run();

        // then
        verify(templateRepository, never()).save(any(Template.class));
        verify(templateRepository, never()).saveAll(anyList());
    }

}
