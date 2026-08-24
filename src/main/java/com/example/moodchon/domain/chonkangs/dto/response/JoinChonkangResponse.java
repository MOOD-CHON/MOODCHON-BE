package com.example.moodchon.domain.chonkangs.dto.response;

public record JoinChonkangResponse(
        Long chonkangId,
        boolean moodDecided,
        boolean isLastParticipant,
        int plannedMemberCount,
        long currentMemberCount,
        boolean hasRecommendedItinerary,
        boolean itineraryCommitted
) {
}
