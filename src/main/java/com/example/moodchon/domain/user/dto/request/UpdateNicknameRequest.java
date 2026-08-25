package com.example.moodchon.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(
        @NotBlank @Size(max = 6) String nickname
) {
}
