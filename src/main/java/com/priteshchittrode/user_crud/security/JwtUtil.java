package com.priteshchittrode.user_crud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "javatechie_secret_javatechie_secret_123456";
    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15 min
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 days

    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String ACCESS = "ACCESS";
    private static final String REFRESH = "REFRESH";

    public String generateAccessToken(Long userId) {
        return generateToken(userId, ACCESS, ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, REFRESH, REFRESH_EXPIRATION);
    }

    private String generateToken(Long userId, String tokenType, long expiration) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(KEY)
                .compact();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(extractAllClaims(token).getSubject());
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
    }

    public boolean isAccessToken(String token) {
        return ACCESS.equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH.equals(extractTokenType(token));
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isValidAccessToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))
                    && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class))
                    && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}