package com.example.moodchon.domain.recommendation.dto.response;

import java.util.List;

public record ItineraryDayResponse(
        int dayNumber,
        List<ItineraryItemResponse> items
) {
}
