package com.example.moodchon.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileImageRequest(
        @NotBlank String profileImageUrl
) {
}
