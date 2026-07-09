package com.example.sprout.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        //CSRF disable
        http.csrf((auth) -> auth.disable());
        //FORM login disable
        http.formLogin((auth) -> auth.disable());
        //HTTP basic 인증방식 disable
        http.httpBasic((auth) -> auth.disable());

        //경로별 권한(인가)관리: 임시로 전체 permit
        http.authorizeHttpRequests((auth) -> auth
                .anyRequest().permitAll());

        //세션 stateless로 유지
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
