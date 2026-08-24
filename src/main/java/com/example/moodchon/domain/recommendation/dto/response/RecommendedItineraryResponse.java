package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import com.example.moodchon.domain.recommendation.entity.RecommendedItineraryItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record RecommendedItineraryResponse(
        Long recommendedItineraryId,
        boolean committed,
        int totalDays,
        List<ItineraryDayResponse> days
) {

    public static RecommendedItineraryResponse of(RecommendedItinerary itinerary,
                                                    List<RecommendedItineraryItem> items) {
        Map<Integer, List<ItineraryItemResponse>> itemsByDay = new LinkedHashMap<>();
        for (RecommendedItineraryItem item : items) {
            itemsByDay.computeIfAbsent(item.getDayNumber(), day -> new ArrayList<>())
                    .add(ItineraryItemResponse.from(item));
        }

        List<ItineraryDayResponse> days = itemsByDay.entrySet().stream()
                .map(entry -> new ItineraryDayResponse(entry.getKey(), entry.getValue()))
                .toList();

        return new RecommendedItineraryResponse(itinerary.getId(), itinerary.isCommitted(), days.size(), days);
    }
}
