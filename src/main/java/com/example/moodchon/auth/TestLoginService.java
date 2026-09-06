package com.example.moodchon.auth;

import com.example.moodchon.auth.dto.TokenResponse;
import com.example.moodchon.auth.jwt.JwtProvider;
import com.example.moodchon.domain.user.entity.AuthProvider;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.entity.UserRole;
import com.example.moodchon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

// 카카오/애플 SDK 토큰 없이 Swagger에서 인증이 필요한 API를 호출하기 위한 로그인.
// testId를 다르게 주면 별도 계정이 만들어져 촌캉스 참여처럼 여러 명이 필요한 시나리오도 테스트할 수 있다.
@Service
@RequiredArgsConstructor
public class TestLoginService {

    private static final String DEFAULT_TEST_ID = "1";

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse login(String testId, String nickname) {
        String providerId = StringUtils.hasText(testId) ? testId.trim() : DEFAULT_TEST_ID;
        String resolvedNickname = StringUtils.hasText(nickname) ? nickname.trim() : "테스트유저" + providerId;

        User user = userRepository.findByProviderAndProviderId(AuthProvider.TEST, providerId)
                .orElseGet(() -> userRepository.save(User.builder()
                        .provider(AuthProvider.TEST)
                        .providerId(providerId)
                        .nickname(resolvedNickname)
                        .role(UserRole.USER)
                        .build()));

        return new TokenResponse(
                jwtProvider.createAccessToken(user.getId(), user.getRole().name()),
                jwtProvider.createRefreshToken(user.getId())
        );
    }
}
