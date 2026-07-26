package com.example.sprout.domain.motivation.service;

import com.example.sprout.domain.motivation.entity.Motivation;
import com.example.sprout.domain.motivation.exception.MotivationErrorCode;
import com.example.sprout.domain.motivation.repository.MotivationRepository;
import com.example.sprout.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MotivationServiceTest {

    @Mock
    private MotivationRepository motivationRepository;

    @InjectMocks
    private MotivationService motivationService;

    private Motivation createMotivation(String content, int displayOrder) {
        return Motivation.builder()
                .content(content)
                .displayOrder(displayOrder)
                .build();
    }

    @Nested
    @DisplayName("오늘의 동기부여 문구 조회")
    class GetDailyMotivation {

        @Test
        @DisplayName("등록된 문구가 있으면 해당 문구 반환")
        void returnMotivationWhenExists() {
            // given
            String content = "동기부여 문구";
            Motivation motivation = createMotivation(content, 1);

            given(motivationRepository.count()).willReturn(15L);
            given(motivationRepository.findByDisplayOrder(anyInt())).willReturn(Optional.of(motivation));

            // when
            Motivation result = motivationService.getDailyMotivation();

            // then
            assertThat(result.getContent()).isEqualTo(content);
        }

        @Test
        @DisplayName("전체 문구 개수 범위 안의 displayOrder로 조회")
        void queryWithDisplayOrderWithinValidRange() {
            // given
            long totalCount = 15L;
            given(motivationRepository.count()).willReturn(totalCount);
            given(motivationRepository.findByDisplayOrder(anyInt()))
                    .willReturn(Optional.of(createMotivation("동기부여 문구", 1)));

            ArgumentCaptor<Integer> orderCaptor = ArgumentCaptor.forClass(Integer.class);

            // when
            motivationService.getDailyMotivation();

            // then
            // displayOrder는 (오늘 날짜 % totalCount)+1 이므로 항상 1 ~ totalCount 사이 범위여야 함
            verify(motivationRepository).findByDisplayOrder(orderCaptor.capture());
            assertThat(orderCaptor.getValue()).isBetween(1, (int) totalCount);
        }

        @Test
        @DisplayName("등록된 문구가 하나도 없으면 MOTIVATION_NOT_FOUND 예외")
        void throwExceptionWhenNoMotivationExists() {
            // given
            given(motivationRepository.count()).willReturn(0L);

            // when & then
            assertThatThrownBy(() -> motivationService.getDailyMotivation())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(MotivationErrorCode.MOTIVATION_NOT_FOUND));
        }

        @Test
        @DisplayName("계산된 displayOrder에 해당하는 문구가 없으면 MOTIVATION_NOT_FOUND 예외")
        void throwExceptionWhenDisplayOrderNotFound() {
            // given
            given(motivationRepository.count()).willReturn(15L);
            given(motivationRepository.findByDisplayOrder(anyInt())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> motivationService.getDailyMotivation())
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(MotivationErrorCode.MOTIVATION_NOT_FOUND));
        }
    }
}
