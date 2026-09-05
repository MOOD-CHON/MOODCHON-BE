package com.example.moodchon.domain.recommendation.ai;

import com.example.moodchon.domain.place.entity.Place;
import java.util.List;

public record ActivityScoreContext(
        String moodName,
        String moodDescription,
        List<Place> candidates
) {
}
