package com.example.sprout.domain.category.initializer;

import com.example.sprout.domain.category.entity.Category;
import com.example.sprout.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    private static final List<String> CATEGORIES = List.of(
            "COLLABORATION", "PROBLEM_SOLVING", "COMMUNICATION",
            "LEADERSHIP", "CHALLENGE", "ACHIEVEMENT", "GROWTH",
            "PROFESSIONAL_SKILLS", "CREATIVITY", "CONFLICT_MANAGEMENT",
            "PLANNING"
    );

   @Override
   @Transactional
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            return;
        }

        categoryRepository.saveAll(
                CATEGORIES.stream()
                        .map(type -> Category.builder()
                                .type(type)
                                .build()
                        )
                        .toList()
        );
    }
}
