package com.example.moodchon.auth.apple;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 애플은 고정된 client_secret 대신, Team ID/Key ID/개인키(.p8)로 서명한 JWT를
 * client_secret으로 요구한다. 만료(최대 6개월) 관리를 피하기 위해 매 요청마다
 * 짧은 수명(5분)의 JWT를 새로 발급한다.
 */
@Component
@RequiredArgsConstructor
public class AppleClientSecretGenerator {

    private static final String APPLE_AUD = "https://appleid.apple.com";
    private static final Duration EXPIRATION = Duration.ofMinutes(5);

    private final AppleOAuthProperties appleOAuthProperties;

    public String generate() {
        try {
            ECPrivateKey privateKey = loadPrivateKey(appleOAuthProperties.privateKey());
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(appleOAuthProperties.teamId())
                    .subject(appleOAuthProperties.clientId())
                    .audience(APPLE_AUD)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(EXPIRATION)))
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(appleOAuthProperties.keyId())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new ECDSASigner(privateKey));

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("애플 client_secret JWT 생성에 실패했습니다.", e);
        }
    }

    private ECPrivateKey loadPrivateKey(String pem) throws Exception {
        String sanitized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(sanitized);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}