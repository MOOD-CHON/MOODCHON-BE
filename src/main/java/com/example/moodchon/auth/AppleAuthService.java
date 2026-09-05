package com.example.moodchon.auth;

import com.example.moodchon.auth.dto.TokenResponse;
import com.example.moodchon.auth.jwt.JwtProvider;
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

// 애플 네이티브 SDK(sign_in_with_apple 등)가 발급한 identityToken을 애플 공개키로 서명 검증한다.
// 검증 대상이 Bundle ID(aud)뿐이라 Services ID/Key(.p8) 없이 App ID만으로 동작한다.
@Service
public class AppleAuthService {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_JWK_SET_URI = "https://appleid.apple.com/auth/keys";

    private final UserProvisioningService userProvisioningService;
    private final JwtProvider jwtProvider;
    private final String bundleId;
    private final JwtDecoder appleJwtDecoder;

    public AppleAuthService(UserProvisioningService userProvisioningService,
                             JwtProvider jwtProvider,
                             @Value("${apple.bundle-id}") String bundleId) {
        this.userProvisioningService = userProvisioningService;
        this.jwtProvider = jwtProvider;
        this.bundleId = bundleId;
        this.appleJwtDecoder = NimbusJwtDecoder.withJwkSetUri(APPLE_JWK_SET_URI).build();
    }

    public TokenResponse login(String identityToken) {
        Jwt jwt = decodeAndVerify(identityToken);

        OAuthAttributes attributes = OAuthAttributes.ofApple(jwt.getClaims());
        User user = userProvisioningService.saveOrUpdate(attributes);

        String jwtAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String jwtRefreshToken = jwtProvider.createRefreshToken(user.getId());
        return new TokenResponse(jwtAccessToken, jwtRefreshToken);
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
