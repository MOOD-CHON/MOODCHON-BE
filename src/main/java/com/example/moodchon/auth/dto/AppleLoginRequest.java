package com.example.moodchon.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
        @NotBlank String identityToken,
        @NotBlank String authorizationCode
) {
}
