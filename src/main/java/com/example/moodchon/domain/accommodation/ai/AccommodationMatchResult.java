package com.example.moodchon.domain.accommodation.ai;

import java.util.List;

public record AccommodationMatchResult(
        List<MatchedAccommodation> accommodations
) {

    public record MatchedAccommodation(long placeId, int matchScore, List<String> tags, List<String> highlights) {
    }
}
