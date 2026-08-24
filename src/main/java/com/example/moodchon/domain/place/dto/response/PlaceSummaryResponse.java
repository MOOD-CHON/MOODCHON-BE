package com.example.moodchon.domain.place.dto.response;

import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.place.entity.TagColor;

public record PlaceSummaryResponse(
        Long id,
        String name,
        PlaceCategory category,
        String categoryLabel,
        TagColor tagColor,
        String address,
        Double latitude,
        Double longitude,
        String thumbnailUrl
) {

    public static PlaceSummaryResponse from(Place place) {
        return new PlaceSummaryResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getCategory().getLabel(),
                place.getCategory().getTagColor(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getThumbnailUrl()
        );
    }
}
