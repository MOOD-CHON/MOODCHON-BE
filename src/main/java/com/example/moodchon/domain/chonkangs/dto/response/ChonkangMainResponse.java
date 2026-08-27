package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationResponse;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMainStatus;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItineraryResponse;
import java.util.List;

public record ChonkangMainResponse(
        ChonkangsMainStatus status,
        MoodProgressResponse moodProgress,
        MoodResultResponse moodResult,
        List<RecommendedAccommodationResponse> recommendedAccommodations,
        ConfirmedAccommodationResponse confirmedAccommodation,
        RecommendedItineraryResponse itinerary
) {

    public static ChonkangMainResponse moodVoting(MoodProgressResponse moodProgress) {
        return new ChonkangMainResponse(ChonkangsMainStatus.MOOD_VOTING, moodProgress, null, null, null, null);
    }

    public static ChonkangMainResponse moodDecided(MoodResultResponse moodResult,
                                                     List<RecommendedAccommodationResponse> recommendedAccommodations) {
        return new ChonkangMainResponse(
                ChonkangsMainStatus.MOOD_DECIDED, null, moodResult, recommendedAccommodations, null, null);
    }

    public static ChonkangMainResponse accommodationConfirmed(MoodResultResponse moodResult,
                                                                ConfirmedAccommodationResponse confirmedAccommodation,
                                                                RecommendedItineraryResponse itinerary) {
        return new ChonkangMainResponse(
                ChonkangsMainStatus.ACCOMMODATION_CONFIRMED, null, moodResult, null, confirmedAccommodation, itinerary);
    }
}
