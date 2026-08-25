package com.example.moodchon.domain.accommodation.external;

public record TourApiAccommodation(
        String contentId,
        String name,
        String address,
        double longitude,
        double latitude,
        String thumbnailUrl
) {
}
