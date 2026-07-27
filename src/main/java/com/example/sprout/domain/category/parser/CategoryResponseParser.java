package com.example.sprout.domain.category.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CategoryResponseParser {
    private static final Pattern DELIMITER = Pattern.compile("[,\\n;/]+");
    private static final Pattern NOISE_CHARS = Pattern.compile("[\"'\\-•*]");

    public static List<String> parse (String raw) {
        if (raw == null || raw.isBlank()) return List.of();

        return Arrays.stream(DELIMITER.split(raw)) //DELIMITER 기준 쪼개기
                .map(NOISE_CHARS::matcher)
                .map(noise -> noise.replaceAll("")) //노이즈 제거
                .map(String::trim)
                .filter(token -> !token.isBlank()) //공백 필터링
                .filter(token -> token.length() <= 20) //문자 형태의 응답 필터링
                .map(token -> token.toUpperCase(Locale.ROOT))
                .distinct() //중복 응답 필터링
                .toList();
    }
}
