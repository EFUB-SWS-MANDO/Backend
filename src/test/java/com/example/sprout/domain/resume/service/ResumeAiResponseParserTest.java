package com.example.sprout.domain.resume.service;

import com.example.sprout.domain.resume.dto.ai.GeneratedAnswer;
import com.example.sprout.domain.resume.parser.ResumeAiResponseParser;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeAiResponseParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResumeAiResponseParser parser = new ResumeAiResponseParser(objectMapper);

    @Test
    @DisplayName("정상 JSON 응답을 파싱한다")
    void parse_success() {
        String content = """
                {
                  "answers": [
                    { "order": 1, "answer": "지원동기입니다", "description": "강조" }
                  ]
                }
                """;

        Map<Long, GeneratedAnswer> result = parser.parse(content);

        assertThat(result.get(1L).answer()).isEqualTo("지원동기입니다");
    }

    @Test
    @DisplayName("코드블록으로 감싸진 응답도 파싱한다")
    void parse_withCodeBlock() {
        String content = """
```json
                {
                  "answers": [
                    { "order": 1, "answer": "답변", "description": "설명" }
                  ]
                }
```
                """;

        Map<Long, GeneratedAnswer> result = parser.parse(content);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("깨진 JSON이면 예외를 던진다")
    void parse_invalidJson_throwsException() {
        String content = "이건 JSON이 아니야";

        assertThatThrownBy(() -> parser.parse(content))
                .isInstanceOf(BusinessException.class);
    }
}