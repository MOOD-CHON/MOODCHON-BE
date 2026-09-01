package com.example.moodchon.domain.place.dto.response;

import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import java.util.List;

public record PlaceDetailResponse(
        Long id,
        String name,
        PlaceCategory category,
        String categoryLabel,
        String address,
        String description,
        List<String> images,
        List<String> tags,
        boolean saved
) {

    public static PlaceDetailResponse of(Place place, List<String> images, List<String> tags, boolean saved) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getCategory(),
                place.getCategory().getLabel(),
                place.getAddress(),
                place.getDescription(),
                images,
                tags,
                saved
        );
    }
}
