package com.example.moodchon.auth;

import com.example.moodchon.auth.dto.TokenResponse;
import com.example.moodchon.auth.entity.RefreshToken;
import com.example.moodchon.auth.jwt.JwtProvider;
import com.example.moodchon.auth.repository.RefreshTokenRepository;
import com.example.moodchon.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 카카오/애플/테스트 로그인과 재발급이 공통으로 사용하는 토큰 발급 지점.
// 발급과 저장을 한곳에 두어 저장이 누락된 리프레시 토큰이 생기지 않게 한다.
@Component
@RequiredArgsConstructor
public class TokenIssuer {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse issue(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(
                new RefreshToken(user.getId(), refreshToken, jwtProvider.getExpiresAt(refreshToken)));

        return new TokenResponse(accessToken, refreshToken);
    }
}
