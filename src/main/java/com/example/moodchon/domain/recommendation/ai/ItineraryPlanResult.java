package com.example.moodchon.domain.recommendation.ai;

import java.util.List;

public record ItineraryPlanResult(
        List<DayPlan> days
) {

    public record DayPlan(int dayNumber, List<ItemPlan> items) {
    }

    public record ItemPlan(long placeId, int order, int moodFitScore, String aiSummary) {
    }
}
