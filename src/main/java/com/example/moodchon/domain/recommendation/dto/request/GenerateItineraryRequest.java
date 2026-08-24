package com.example.moodchon.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateItineraryRequest(
        @NotNull Long accommodationPlaceId,
        @NotBlank String moodDescription
) {
}
