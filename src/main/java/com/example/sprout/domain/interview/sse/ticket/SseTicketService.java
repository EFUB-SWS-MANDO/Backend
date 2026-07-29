package com.example.sprout.domain.interview.sse.ticket;

import com.example.sprout.domain.interview.exception.InterviewErrorCode;
import com.example.sprout.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SseTicketService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "sse:ticket:";
    private static final Duration TICKET_TTL = Duration.ofSeconds(60);

    // 티켓 발급 (유효 기간 60초)
    public String issueTicket(Long interviewSessionId) {
        String ticket = UUID.randomUUID().toString();
        String value = interviewSessionId.toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + ticket, value, TICKET_TTL);
        return ticket;
    }

    // 티켓 검증 및 즉시 폐기 (1회용)
    public void verifyAndConsumeTicket(String ticket, Long interviewSessionId) {
        String key = KEY_PREFIX + ticket;
        String value = redisTemplate.opsForValue().getAndDelete(key);

        // 만료되었거나 이미 사용된 티켓이면 인증(401) 예외
        if (value == null) {
            throw new BusinessException(InterviewErrorCode.INVALID_SSE_TICKET);
        }

        Long savedSessionId = Long.parseLong(value);

        // 저장된 세션 ID가 요청한 세션 ID와 일치하지 않으면 권한 없음(403) 예외
        if (!savedSessionId.equals(interviewSessionId)) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_ACCESS_DENIED);
        }
    }

}
