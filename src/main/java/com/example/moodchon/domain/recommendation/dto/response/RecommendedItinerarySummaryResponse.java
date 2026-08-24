package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;

public record RecommendedItinerarySummaryResponse(
        boolean exists,
        boolean committed
) {

    public static RecommendedItinerarySummaryResponse none() {
        return new RecommendedItinerarySummaryResponse(false, false);
    }

    public static RecommendedItinerarySummaryResponse of(RecommendedItinerary itinerary) {
        return new RecommendedItinerarySummaryResponse(true, itinerary.isCommitted());
    }
}
