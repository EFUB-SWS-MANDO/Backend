package com.example.sprout.domain.category.cache;

import com.example.sprout.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class CategoryCacheWarmUp {

    private final CategoryService categoryService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        log.info("[Warm Up] Category 캐시 Loading");
        categoryService.getCategories();
    }

}
