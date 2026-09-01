package com.example.moodchon.domain.place.ai;

import com.example.moodchon.domain.place.entity.PlaceCategory;
import java.util.List;

public record PostTagContext(
        String placeName,
        PlaceCategory category,
        String description,
        List<String> availableTagNames
) {
}
