package com.example.moodchon.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddPlaceItineraryItemRequest(
        @NotNull Long placeId
) {
}
