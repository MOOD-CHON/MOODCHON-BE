package com.example.moodchon.domain.accommodation.dto.response;

import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.place.entity.Place;
import java.util.List;

public record RecommendedAccommodationResponse(
        Long placeId,
        String name,
        String address,
        String thumbnailUrl,
        String description,
        List<String> images,
        int matchScore,
        List<String> tags,
        List<String> highlights
) {

    public static RecommendedAccommodationResponse of(RecommendedAccommodation recommendedAccommodation,
                                                        List<String> images) {
        Place place = recommendedAccommodation.getPlace();
        return new RecommendedAccommodationResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getThumbnailUrl(),
                place.getDescription(),
                images,
                recommendedAccommodation.getMatchScore(),
                recommendedAccommodation.getTags(),
                recommendedAccommodation.getHighlights()
        );
    }
}
