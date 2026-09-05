package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.place.dto.response.PlaceSummaryResponse;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import com.example.moodchon.domain.recommendation.entity.TransportMode;
import java.util.List;

public record ItineraryItemDetailResponse(
        Long itemId,
        PlaceSummaryResponse place,
        int moodFitScore,
        String aiSummary,
        TransportMode transportMode,
        int travelMinutes,
        List<String> tags,
        String description,
        List<String> images,
        GeneralPlaceDetailInfo generalInfo,
        EventPlaceDetailInfo eventInfo,
        RestaurantPlaceDetailInfo restaurantInfo,
        ShoppingPlaceDetailInfo shoppingInfo
) {

    public static ItineraryItemDetailResponse of(RecommendedItineraryItem item, List<String> tags,
                                                  List<String> images, GeneralPlaceDetailInfo generalInfo,
                                                  EventPlaceDetailInfo eventInfo,
                                                  RestaurantPlaceDetailInfo restaurantInfo,
                                                  ShoppingPlaceDetailInfo shoppingInfo) {
        return new ItineraryItemDetailResponse(
                item.getId(),
                PlaceSummaryResponse.from(item.getPlace()),
                item.getMoodFitScore(),
                item.getAiSummary(),
                item.getTransportMode(),
                item.getTravelMinutes(),
                tags,
                item.getPlace().getDescription(),
                images,
                generalInfo,
                eventInfo,
                restaurantInfo,
                shoppingInfo
        );
    }
}
