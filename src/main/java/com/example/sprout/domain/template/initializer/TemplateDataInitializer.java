package com.example.sprout.domain.template.initializer;

import com.example.sprout.domain.template.entity.Template;
import com.example.sprout.domain.template.entity.TemplateValue;
import com.example.sprout.domain.template.enums.TemplateType;
import com.example.sprout.domain.template.repository.TemplateRepository;
import com.example.sprout.domain.template.repository.TemplateValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class TemplateDataInitializer implements CommandLineRunner {

    private final TemplateRepository templateRepository;
    private final TemplateValueRepository templateValueRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (templateRepository.count() > 0) {
            return;
        }

        Template template = templateRepository.save(
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

    }

}
