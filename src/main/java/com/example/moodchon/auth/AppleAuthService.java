package com.example.moodchon.auth;

import com.example.moodchon.auth.dto.TokenResponse;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 애플 네이티브 SDK(sign_in_with_apple 등)가 발급한 identityToken을 애플 공개키로 서명 검증한다.
// 검증 대상이 Bundle ID(aud)뿐이라 Services ID/Key(.p8) 없이 App ID만으로 동작한다.
@Service
public class AppleAuthService {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWK_SET_URI = "https://appleid.apple.com/auth/keys";

    private final UserProvisioningService userProvisioningService;
    private final TokenIssuer tokenIssuer;
    private final AppleTokenClient appleTokenClient;
    private final String bundleId;
    private final JwtDecoder appleJwtDecoder;

    public AppleAuthService(UserProvisioningService userProvisioningService,
                             TokenIssuer tokenIssuer,
                             AppleTokenClient appleTokenClient,
                             @Value("${apple.bundle-id}") String bundleId) {
        this.userProvisioningService = userProvisioningService;
        this.tokenIssuer = tokenIssuer;
        this.appleTokenClient = appleTokenClient;
        this.bundleId = bundleId;
        this.appleJwtDecoder = NimbusJwtDecoder.withJwkSetUri(APPLE_JWK_SET_URI).build();
    }

    // saveOrUpdate로 조회/생성한 user가 메서드 종료 시까지 영속 상태로 남아있어야
    // updateAppleRefreshToken()으로 바뀐 값이 트랜잭션 커밋 시점에 실제로 반영된다.
    @Transactional
    public TokenResponse login(String identityToken, String authorizationCode) {
        Jwt jwt = decodeAndVerify(identityToken);

        OAuthAttributes attributes = OAuthAttributes.ofApple(jwt.getClaims());
        User user = userProvisioningService.saveOrUpdate(attributes);

        // 탈퇴 시 연결을 철회(auth/revoke)하려면 이 refresh_token이 있어야 한다.
        // 매 로그인마다 새로 교환해 최신 값으로 덮어쓴다.
        String appleRefreshToken = appleTokenClient.exchangeAuthorizationCode(authorizationCode);
        user.updateAppleRefreshToken(appleRefreshToken);

        return tokenIssuer.issue(user);
    }

    private Jwt decodeAndVerify(String identityToken) {
        try {
            Jwt jwt = appleJwtDecoder.decode(identityToken);

            boolean issuerValid = APPLE_ISSUER.equals(jwt.getClaimAsString("iss"));
            List<String> audience = jwt.getAudience();
            boolean audienceValid = audience != null && audience.contains(bundleId);

            if (!issuerValid || !audienceValid) {
                throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
            }

            return jwt;
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}
