package com.example.moodchon.auth;

import com.example.moodchon.auth.jwt.JwtProvider;
import com.example.moodchon.auth.repository.RefreshTokenRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 저장된 리프레시 토큰을 지워 해당 기기의 재발급을 차단한다.
// 액세스 토큰은 만료 전까지 유효하므로 클라이언트가 함께 폐기해야 한다.
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public void logout(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken) || !jwtProvider.isRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        refreshTokenRepository.deleteByToken(refreshToken);
    }
}
