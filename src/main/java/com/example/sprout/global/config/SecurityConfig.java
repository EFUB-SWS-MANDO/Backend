package com.example.sprout.global.config;

import com.example.sprout.domain.auth.jwt.JwtFilter;
import com.example.sprout.domain.auth.jwt.JwtUtil;
import com.example.sprout.domain.auth.security.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CSRF disable
        http.csrf((auth) -> auth.disable());
        //FORM login disable
        http.formLogin((auth) -> auth.disable());
        //HTTP basic 인증방식 disable
        http.httpBasic((auth) -> auth.disable());

        //경로별 권한(인가)관리: 임시로 전체 permit
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/api/auth/sign-in",
                        "/", "/api/interviews/{interviewSessionId}/stream",
                        "/api/auth/refresh", "/api/test/**").permitAll()
                .anyRequest().authenticated());

        //필터 등록
        http.addFilterBefore(new JwtFilter(jwtUtil, redisTemplate), UsernamePasswordAuthenticationFilter.class);

        //세션 stateless로 유지
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(e -> e
                .authenticationEntryPoint(customAuthenticationEntryPoint)
        );

        return http.build();
    }
}
