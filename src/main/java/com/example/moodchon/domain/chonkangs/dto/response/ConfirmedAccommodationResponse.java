package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.place.entity.Place;

public record ConfirmedAccommodationResponse(
        Long placeId,
        String name,
        String address,
        String thumbnailUrl
) {

    public static ConfirmedAccommodationResponse of(Place place) {
        return new ConfirmedAccommodationResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getThumbnailUrl()
        );
    }
}
