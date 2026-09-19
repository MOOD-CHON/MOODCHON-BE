package com.example.moodchon.auth.controller;

import com.example.moodchon.auth.AppleAuthService;
import com.example.moodchon.auth.KakaoAuthService;
import com.example.moodchon.auth.LogoutService;
import com.example.moodchon.auth.TestLoginService;
import com.example.moodchon.auth.TokenRefreshService;
import com.example.moodchon.auth.dto.AppleLoginRequest;
import com.example.moodchon.auth.dto.KakaoLoginRequest;
import com.example.moodchon.auth.dto.KakaoWebLoginRequest;
import com.example.moodchon.auth.dto.LogoutRequest;
import com.example.moodchon.auth.dto.TestLoginRequest;
import com.example.moodchon.auth.dto.TokenRefreshRequest;
import com.example.moodchon.auth.dto.TokenResponse;
import com.example.moodchon.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoAuthService kakaoAuthService;
    private final AppleAuthService appleAuthService;
    private final TokenRefreshService tokenRefreshService;
    private final TestLoginService testLoginService;
    private final LogoutService logoutService;

    @PostMapping("/kakao")
    public ApiResponse<TokenResponse> loginWithKakao(@Valid @RequestBody KakaoLoginRequest request) {
        return ApiResponse.success(kakaoAuthService.login(request.accessToken()));
    }

    // 웹은 카카오 SDK가 브라우저에서 토큰 발급을 막아놔 인가 코드만 넘어온다.
    @PostMapping("/kakao/web")
    public ApiResponse<TokenResponse> loginWithKakaoOnWeb(@Valid @RequestBody KakaoWebLoginRequest request) {
        return ApiResponse.success(
                kakaoAuthService.loginWithAuthorizationCode(request.code(), request.redirectUri()));
    }

    @PostMapping("/apple")
    public ApiResponse<TokenResponse> loginWithApple(@Valid @RequestBody AppleLoginRequest request) {
        return ApiResponse.success(appleAuthService.login(request.identityToken(), request.authorizationCode()));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.success(tokenRefreshService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        logoutService.logout(request.refreshToken());
        return ApiResponse.success();
    }

    // 소셜 SDK 없이 Swagger에서 API를 호출하기 위한 테스트용 로그인. 운영 배포 전 제거 필요.
    @PostMapping("/test-login")
    public ApiResponse<TokenResponse> testLogin(@RequestBody(required = false) TestLoginRequest request) {
        String testId = request != null ? request.testId() : null;
        String nickname = request != null ? request.nickname() : null;
        return ApiResponse.success(testLoginService.login(testId, nickname));
    }
}
