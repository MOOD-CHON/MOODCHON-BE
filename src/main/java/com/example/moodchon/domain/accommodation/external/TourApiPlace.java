package com.example.moodchon.domain.accommodation.external;

public record TourApiPlace(
        String contentId,
        String name,
        String address,
        double longitude,
        double latitude,
        String thumbnailUrl
) {
}
