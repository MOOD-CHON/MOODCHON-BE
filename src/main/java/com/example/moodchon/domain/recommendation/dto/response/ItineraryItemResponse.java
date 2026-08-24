package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.place.dto.response.PlaceSummaryResponse;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.entity.TransportMode;

public record ItineraryItemResponse(
        Long itemId,
        int order,
        PlaceSummaryResponse place,
        int moodFitScore,
        String aiSummary,
        TransportMode transportMode,
        int travelMinutes
) {

    public static ItineraryItemResponse from(RecommendedItineraryItem item) {
        return new ItineraryItemResponse(
                item.getId(),
                item.getOrderInDay(),
                PlaceSummaryResponse.from(item.getPlace()),
                item.getMoodFitScore(),
                item.getAiSummary(),
                item.getTransportMode(),
                item.getTravelMinutes()
        );
    }
}
