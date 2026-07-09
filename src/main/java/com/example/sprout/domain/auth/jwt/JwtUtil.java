package com.example.sprout.domain.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
}
