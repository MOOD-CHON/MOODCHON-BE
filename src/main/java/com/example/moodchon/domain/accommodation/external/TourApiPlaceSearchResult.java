package com.example.moodchon.domain.accommodation.external;

import com.example.moodchon.domain.place.entity.PlaceCategory;

public record TourApiPlaceSearchResult(
        TourApiPlace place,
        PlaceCategory category
) {
}
