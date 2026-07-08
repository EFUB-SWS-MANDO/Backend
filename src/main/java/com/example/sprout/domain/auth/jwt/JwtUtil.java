package com.example.sprout.domain.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final Long accessExpirationMs;
    private final Long refreshExpirationMs;

    public JwtUtil (@Value("${jwt.secret}")String secret,
                    @Value("${jwt.access-expiration}")Long accessExpirationMs,
                    @Value("${jwt.refresh-expiration}")Long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, accessExpirationMs);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, refreshExpirationMs);
    }

    private String createToken(Long memberId, Long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime()+expirationMs);

        return Jwts.builder()
                .claim("memberId", memberId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Long getAccessExpirationMs() {return accessExpirationMs;}

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (ExpiredJwtException e) {
            //만료된 토큰
            return false;
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            //형식이 잘못된 경우/서명이 다른 경우/미지원 토큰
            return false;
        } catch (IllegalArgumentException e) {
            //토큰이 비거나 null인 경우
            return false;
        }
    }

    //토큰에서 memberId 추출
    public Long getMemberId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("memberId", Long.class);
    }

    //HTTP 요청 헤더에서 토큰 추출
    public static String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }
}
