package com.example.sprout.domain.member.service;

import com.example.sprout.domain.member.exception.MemberErrorCode;
import com.example.sprout.domain.member.repository.MemberRepository;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@Transactional
public class MemberVisitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;

    public void checkVisit(Long memberId) {
        if(!isFirstVisitToday(memberId)) return;

        try {
            memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND))
                    .updateVisitStreak();
        } catch (Exception e) {
            rollbackVisitMark(memberId);
            throw e;
        }

    }

    private String visitKey(Long memberId) {
        return "visit:" + memberId + ":" + LocalDate.now();
    }

    private boolean isFirstVisitToday(Long memberId) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(visitKey(memberId), "true", Duration.ofDays(1));
        return Boolean.TRUE.equals(result);
    }

    private void rollbackVisitMark(Long memberId) {
        redisTemplate.delete(visitKey(memberId));
    }
}
