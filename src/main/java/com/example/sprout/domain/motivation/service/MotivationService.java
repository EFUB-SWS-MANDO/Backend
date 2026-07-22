package com.example.sprout.domain.motivation.service;

import com.example.sprout.domain.motivation.entity.Motivation;
import com.example.sprout.domain.motivation.exception.MotivationErrorCode;
import com.example.sprout.domain.motivation.repository.MotivationRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MotivationService {

    private final MotivationRepository motivationRepository;

    @Cacheable(
            value = "dailyMotivation",
            key = "T(java.time.LocalDate).now().toString()"
    )
    public Motivation getDailyMotivation() {
        // 현재 날짜를 기준으로 문구 순서 계산 (하루 동안 모든 사용자에게 동일한 문구 제공)
        // 캐시 key로 매일 날짜를 사용해 이전 일의 캐시가 다음 날까지 사용되지 않도록 분리

        long totalCount = motivationRepository.count();

        if (totalCount == 0) {
            throw new BusinessException(MotivationErrorCode.MOTIVATION_NOT_FOUND);
        }

        int displayOrder = (int) (LocalDate.now().toEpochDay() % totalCount) + 1;

        return motivationRepository.findByDisplayOrder(displayOrder)
                .orElseThrow(() -> new BusinessException(MotivationErrorCode.MOTIVATION_NOT_FOUND));
    }


}
