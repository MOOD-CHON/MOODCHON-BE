package com.example.moodchon.auth;

import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 애플 토큰 교환(auth/token)·철회(auth/revoke) API 호출에 필요한 client_secret(JWT)을
// 호출 시점마다 짧은 수명으로 즉석에서 생성한다. 네이티브 로그인 자체(identityToken 검증)에는
// 필요 없고, authorizationCode를 애플 서버와 직접 주고받는 이 두 경우에만 쓰인다.
@Component
public class AppleClientSecretGenerator {

    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";

    private final String teamId;
    private final String keyId;
    private final String bundleId;
    private final String privateKeyBase64;
    private volatile PrivateKey privateKey;

    public AppleClientSecretGenerator(
            @Value("${apple.team-id}") String teamId,
            @Value("${apple.key-id}") String keyId,
            @Value("${apple.bundle-id}") String bundleId,
            @Value("${apple.private-key-base64}") String privateKeyBase64) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.bundleId = bundleId;
        this.privateKeyBase64 = privateKeyBase64;
    }

    public String generate() {
        Instant now = Instant.now();

        return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .issuer(teamId)
                .setAudience(APPLE_AUDIENCE)
                .subject(bundleId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(5, ChronoUnit.MINUTES)))
                .signWith(privateKey())
                .compact();
    }

    // 키 파싱을 빈 생성 시점이 아니라 최초 사용 시점으로 미룬다. CI 테스트 등 애플 키가
    // 설정되지 않은 환경에서도 애플 로그인을 실제로 쓰지 않는 한 앱/컨텍스트가 정상 기동해야 한다.
    private PrivateKey privateKey() {
        if (privateKey == null) {
            privateKey = parsePrivateKey(privateKeyBase64);
        }
        return privateKey;
    }

    private static PrivateKey parsePrivateKey(String privateKeyBase64) {
        try {
            String pem = new String(Base64.getDecoder().decode(privateKeyBase64))
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(pem);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new IllegalStateException("애플 private key 파싱에 실패했습니다.", e);
        }
    }
}
