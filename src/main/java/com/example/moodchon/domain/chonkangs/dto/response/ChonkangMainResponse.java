package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationResponse;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMainStatus;
import com.example.moodchon.domain.recommendation.dto.response.RecommendedItineraryResponse;
import java.time.LocalDate;
import java.util.List;

public record ChonkangMainResponse(
        String name,
        LocalDate startDate,
        LocalDate endDate,
        ChonkangsMainStatus status,
        MoodProgressResponse moodProgress,
        MoodResultResponse moodResult,
        List<RecommendedAccommodationResponse> recommendedAccommodations,
        ConfirmedAccommodationResponse confirmedAccommodation,
        RecommendedItineraryResponse itinerary
) {

    public static ChonkangMainResponse moodVoting(String name, LocalDate startDate, LocalDate endDate,
                                                    MoodProgressResponse moodProgress) {
        return new ChonkangMainResponse(
                name, startDate, endDate, ChonkangsMainStatus.MOOD_VOTING, moodProgress, null, null, null, null);
    }

    public static ChonkangMainResponse moodDecided(String name, LocalDate startDate, LocalDate endDate,
                                                     MoodResultResponse moodResult,
                                                     List<RecommendedAccommodationResponse> recommendedAccommodations) {
        return new ChonkangMainResponse(name, startDate, endDate,
                ChonkangsMainStatus.MOOD_DECIDED, null, moodResult, recommendedAccommodations, null, null);
    }

    public static ChonkangMainResponse accommodationConfirmed(String name, LocalDate startDate, LocalDate endDate,
                                                                MoodResultResponse moodResult,
                                                                ConfirmedAccommodationResponse confirmedAccommodation,
                                                                RecommendedItineraryResponse itinerary) {
        return new ChonkangMainResponse(name, startDate, endDate,
                ChonkangsMainStatus.ACCOMMODATION_CONFIRMED, null, moodResult, null, confirmedAccommodation, itinerary);
    }
}
