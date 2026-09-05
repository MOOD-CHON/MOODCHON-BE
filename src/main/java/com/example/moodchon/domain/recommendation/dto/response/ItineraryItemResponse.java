package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.place.dto.response.PlaceSummaryResponse;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.entity.TransportMode;

// place가 null이면 "장소 없이 추가하기"로 만든 항목 — customName/customCategoryLabel/customTagColor로 대신 표시한다.
public record ItineraryItemResponse(
        Long itemId,
        int order,
        PlaceSummaryResponse place,
        String customName,
        String customCategoryLabel,
        String customTagColor,
        Integer moodFitScore,
        String aiSummary,
        TransportMode transportMode,
        Integer travelMinutes
) {

    public static ItineraryItemResponse from(RecommendedItineraryItem item) {
        boolean custom = item.isCustom();
        return new ItineraryItemResponse(
                item.getId(),
                item.getOrderInDay(),
                custom ? null : PlaceSummaryResponse.from(item.getPlace()),
                custom ? item.getCustomName() : null,
                custom ? item.getCustomCategory().getLabel() : null,
                custom ? item.getCustomCategory().getTagColor().name() : null,
                custom ? null : item.getMoodFitScore(),
                item.getAiSummary(),
                item.getTransportMode(),
                item.getTravelMinutes()
        );
    }
}
