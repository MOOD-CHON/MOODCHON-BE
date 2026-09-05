package com.example.moodchon.domain.recommendation.ai;

import java.util.List;

public record ActivityScoreResult(
        List<ScoredPlace> items
) {

    public record ScoredPlace(long placeId, int moodFitScore, String aiSummary) {
    }
}
