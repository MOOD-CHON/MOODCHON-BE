package com.example.moodchon.auth;

import com.example.moodchon.auth.dto.TokenResponse;
import com.example.moodchon.auth.jwt.JwtProvider;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 리프레시 토큰을 검증해 토큰을 재발급한다. 액세스 토큰만 갱신하면 리프레시 토큰 만료 시점에 재로그인이 강제되므로
// 리프레시 토큰도 함께 새로 발급한다.
@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 탈퇴한 회원은 @SQLRestriction으로 조회되지 않아 재발급이 막힌다.
        User user = userRepository.findById(jwtProvider.getUserId(refreshToken))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        return new TokenResponse(
                jwtProvider.createAccessToken(user.getId(), user.getRole().name()),
                jwtProvider.createRefreshToken(user.getId())
        );
    }
}
