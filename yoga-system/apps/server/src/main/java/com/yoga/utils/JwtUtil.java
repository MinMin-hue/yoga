package com.yoga.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具
 */
@Component
public class JwtUtil {

    @Value("${yoga.jwt.secret}")
    private String secret;

    @Value("${yoga.jwt.expire-hours:24}")
    private long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("username", username);
        claims.put("role", role);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireHours * 3600 * 1000))
                .signWith(key())
                .compact();
    }

    public ParsedToken parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
        ParsedToken p = new ParsedToken();
        Object uid = claims.get("uid");
        p.setUserId(uid == null ? null : Long.valueOf(uid.toString()));
        p.setUsername(claims.get("username", String.class));
        p.setRole(claims.get("role", String.class));
        return p;
    }

    @Data
    public static class ParsedToken {
        private Long userId;
        private String username;
        private String role;
    }
}
