package com.example.sprout.domain.template.service;

import com.example.sprout.domain.template.dto.TemplateDto;
import com.example.sprout.domain.template.entity.Template;
import com.example.sprout.domain.template.entity.TemplateValue;
import com.example.sprout.domain.template.enums.TemplateType;
import com.example.sprout.domain.template.repository.TemplateRepository;
import com.example.sprout.domain.template.repository.TemplateValueRepository;
import com.example.sprout.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

public class TemplateServiceTest extends IntegrationTestSupport {

    @Autowired
    private TemplateService templateService;

    @MockitoSpyBean
    private TemplateValueRepository templateValueRepository;

    @MockitoSpyBean
    private TemplateRepository templateRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    Template template;

    @BeforeEach
    void setUp() {
        templateRepository.deleteAll();

        template = templateRepository.save(
                Template.builder().type(TemplateType.BASIC).build()
        );

        templateValueRepository.saveAll(
                Stream.of("기본 정보", "활동 내용", "성찰 및 성장", "증빙 자료")
                        .map(value -> TemplateValue.builder()
                                .template(template)
                                .value(value)
                                .build()
                        )
                        .toList()
        );

        clearInvocations(templateValueRepository);
        clearInvocations(templateRepository);
    }

    @Test
    @DisplayName("첫 조회 - DB 조회, 두 번째 조회 - 캐시 조회")
    void getTemplate() {
        // when
        TemplateDto first = templateService.getTemplate(TemplateType.BASIC);

        await()
                .atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(20))
                .untilAsserted(() -> {
                    Boolean hasKey = redisTemplate.hasKey("templates::BASIC");
                    assertThat(hasKey).isTrue();
                });

        TemplateDto second = templateService.getTemplate(TemplateType.BASIC);

        // then
        assertThat(first.values()).isEqualTo(second.values());
        verify(templateRepository, times(1)).findByType(TemplateType.BASIC);
        verify(templateValueRepository, times(1)).findAllByTemplateId(template.getId());
    }

}
