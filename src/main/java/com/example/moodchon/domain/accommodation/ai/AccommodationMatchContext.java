package com.example.moodchon.domain.accommodation.ai;

import com.example.moodchon.domain.place.entity.Place;
import java.util.List;

public record AccommodationMatchContext(
        String moodName,
        String moodDescription,
        List<Place> candidates
) {
}
