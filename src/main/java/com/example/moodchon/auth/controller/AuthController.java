package com.example.moodchon.auth.controller;

import com.example.moodchon.auth.KakaoAuthService;
import com.example.moodchon.auth.dto.KakaoLoginRequest;
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

    @PostMapping("/kakao")
    public ApiResponse<TokenResponse> loginWithKakao(@Valid @RequestBody KakaoLoginRequest request) {
        return ApiResponse.success(kakaoAuthService.login(request.accessToken()));
    }
}
