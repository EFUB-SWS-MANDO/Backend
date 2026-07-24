package com.example.sprout.domain.resume.dto.ai;

import com.example.sprout.domain.resume.service.ResumeService;

import java.util.List;

public record AiAnswerListDto(
        List<AiAnswerDto> answers
) {}
