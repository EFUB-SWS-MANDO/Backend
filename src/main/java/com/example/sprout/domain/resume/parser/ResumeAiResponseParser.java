package com.example.sprout.domain.resume.parser;

import com.example.sprout.domain.resume.dto.ai.AiAnswerDto;
import com.example.sprout.domain.resume.dto.ai.AiAnswerListDto;
import com.example.sprout.domain.resume.dto.ai.GeneratedAnswer;
import com.example.sprout.domain.resume.exception.ResumeErrorCode;
import com.example.sprout.global.error.BusinessException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeAiResponseParser {

    private final ObjectMapper objectMapper;

    public Map<Long, GeneratedAnswer> parse(String content) {

        try {

            String cleaned = content.trim();

            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceFirst("^```json\\s*", "");
                cleaned = cleaned.replaceFirst("^```\\s*", "");
                cleaned = cleaned.replaceFirst("\\s*```$", "");
            }

            AiAnswerListDto parsed =
                    objectMapper.readValue(cleaned, AiAnswerListDto.class);

            return parsed.answers().stream()
                    .collect(Collectors.toMap(
                            a -> (long) a.order(),
                            a -> new GeneratedAnswer(a.answer(), a.description())
                    ));

        } catch (Exception e) {

            log.error("AI 응답 파싱 실패 : {}", content, e);

            throw new BusinessException(
                    ResumeErrorCode.AI_RESPONSE_PARSE_FAILED
            );
        }
    }
}