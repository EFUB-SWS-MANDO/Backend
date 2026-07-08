package com.example.sprout.domain.auth.jwt;

import com.example.sprout.domain.auth.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {this.jwtUtil = jwtUtil;}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = JwtUtil.resolveToken(request);

        if (token == null) {
            log.debug("토큰 없음 - URI: {}", request.getRequestURI());
        }
        else{
            Claims claims = jwtUtil.validateToken(token);

            if (claims == null) log.warn("유효하지 않은 토큰 - URI: {}", request.getRequestURI());
            else {
                Long memberId = claims.get("memberId", Long.class);

                CustomUserDetails userDetails = new CustomUserDetails(memberId);
                Authentication authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("인증 성공 - memberId: {}, URI: {}", memberId, request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }
}
