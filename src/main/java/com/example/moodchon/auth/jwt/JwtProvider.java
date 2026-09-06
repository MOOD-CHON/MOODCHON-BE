package com.example.moodchon.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String role) {
        return createToken(userId, role, TYPE_ACCESS, jwtProperties.accessTokenExpiration());
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, null, TYPE_REFRESH, jwtProperties.refreshTokenExpiration());
    }

    private String createToken(Long userId, String role, String type, long expiration) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        // 같은 사용자가 같은 초에 두 번 발급받으면 클레임이 모두 같아 토큰 문자열까지 동일해진다.
        // 저장소의 token 유니크 제약과 충돌하므로 매 발급마다 고유한 jti를 넣는다.
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .claim(CLAIM_TYPE, type)
                .signWith(secretKey);

        if (role != null) {
            builder.claim(CLAIM_ROLE, role);
        }

        return builder.compact();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    // 리프레시 토큰으로 일반 API를 호출하는 것을 막기 위해 타입을 구분한다.
    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(getType(token));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(getType(token));
    }

    public LocalDateTime getExpiresAt(String token) {
        return LocalDateTime.ofInstant(parseClaims(token).getExpiration().toInstant(), ZoneId.systemDefault());
    }

    private String getType(String token) {
        return parseClaims(token).get(CLAIM_TYPE, String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}