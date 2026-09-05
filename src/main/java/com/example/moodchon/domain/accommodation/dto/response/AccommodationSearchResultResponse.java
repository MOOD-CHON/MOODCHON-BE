package com.example.moodchon.domain.accommodation.dto.response;

import com.example.moodchon.domain.place.entity.Place;

public record AccommodationSearchResultResponse(
        Long placeId,
        String name,
        String address,
        String thumbnailUrl
) {

    public static AccommodationSearchResultResponse from(Place place) {
        return new AccommodationSearchResultResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getThumbnailUrl()
        );
    }
}
