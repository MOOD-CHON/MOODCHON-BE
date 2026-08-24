package com.example.moodchon.domain.recommendation.ai;

import com.example.moodchon.domain.place.entity.Place;
import java.util.List;

public record ItineraryPlanContext(
        String moodName,
        String moodDescription,
        int totalDays,
        Place accommodation,
        List<Place> candidates
) {
}
