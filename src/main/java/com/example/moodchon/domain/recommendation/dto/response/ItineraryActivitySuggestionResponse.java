package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.TagColor;
import com.example.moodchon.domain.recommendation.entity.TransportMode;

// 9.3.1-1 "무드촌이 추천하는 활동" 카드.
public record ItineraryActivitySuggestionResponse(
        Long placeId,
        String name,
        String categoryLabel,
        TagColor tagColor,
        String thumbnailUrl,
        int moodFitScore,
        String aiSummary,
        TransportMode transportMode,
        int travelMinutes
) {

    public static ItineraryActivitySuggestionResponse of(Place place, int moodFitScore, String aiSummary,
                                                          TransportMode transportMode, int travelMinutes) {
        return new ItineraryActivitySuggestionResponse(
                place.getId(),
                place.getName(),
                place.getCategory().getLabel(),
                place.getCategory().getTagColor(),
                place.getThumbnailUrl(),
                moodFitScore,
                aiSummary,
                transportMode,
                travelMinutes
        );
    }
}
