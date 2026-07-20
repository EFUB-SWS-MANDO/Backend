package com.example.sprout.domain.template.controller;

import com.example.sprout.domain.auth.security.AuthMember;
import com.example.sprout.domain.template.dto.TemplateDto;
import com.example.sprout.domain.template.enums.TemplateType;
import com.example.sprout.domain.template.service.TemplateService;
import com.example.sprout.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/templates")
@Slf4j
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<ApiResponse<TemplateDto>> getTemplate(
            @AuthMember Long memberId,
            @RequestParam(name = "type", defaultValue = "BASIC") TemplateType type
            ) {
        log.info("template 조회 요청, requesterId={}", memberId);

        TemplateDto response = templateService.getTemplate(type);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "템플릿 조회 성공",
                        response
                )
        );
    }

}
