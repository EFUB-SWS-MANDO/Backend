package com.example.sprout.global.ai.prompt;

import com.example.sprout.global.ai.exception.AiCallException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class PromptTemplateLoader {

    public PromptTemplate load(String path) {
        try (InputStream is = new ClassPathResource("prompts/" + path).getInputStream()) {
            String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new PromptTemplate(raw);
        } catch (IOException e) {
            throw new AiCallException("프롬프트 파일 로드 실패: " + path, e);
        }
    }

    public record PromptTemplate(String raw) {
        public String render(Map<String, String> values) {
            String result = raw;
            for (var entry : values.entrySet()) {
                result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
            return result;
        }
    }
}
