package com.priteshchittrode.user_crud.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "javatechie_secret";

    private final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15 min
    private final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days

    // 🔐 ACCESS TOKEN
    public String generateAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 🔁 REFRESH TOKEN
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 🆔 Extract USER ID
    public Long extractUserId(String token) {
        return Long.parseLong(
                Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject()
        );
    }

    public boolean isTokenExpired(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
