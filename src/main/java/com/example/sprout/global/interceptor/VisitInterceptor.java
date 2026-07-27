package com.example.sprout.global.interceptor;

import com.example.sprout.domain.auth.security.CustomUserDetails;
import com.example.sprout.domain.member.service.MemberVisitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class VisitInterceptor implements HandlerInterceptor {

    private final MemberVisitService memberVisitService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않은 요청은 방문 기록 대상이 아니므로 그대로 통과
        if(authentication == null || !authentication.isAuthenticated()) return true;

        // CustomDetails를 사용하는 인증만 방문 기록 처리
        if(!(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) return true;

        // 인증된 사용자의 방문 여부 확인 및 필요 시 기록 갱신
        memberVisitService.checkVisit(customUserDetails.getMemberId());

        return true;
    }
}
