package com.example.moodchon.auth.dto;

import jakarta.validation.constraints.NotBlank;

// 웹 카카오 로그인. redirectUri는 인가 코드를 받을 때 쓴 값과 정확히 같아야 카카오가 교환을 허용한다.
public record KakaoWebLoginRequest(
        @NotBlank String code,
        @NotBlank String redirectUri
) {
}
