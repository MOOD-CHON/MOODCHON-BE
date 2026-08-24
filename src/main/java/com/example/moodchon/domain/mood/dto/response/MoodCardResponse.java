package com.example.moodchon.domain.mood.dto.response;

import com.example.moodchon.domain.mood.entity.MoodCard;
import com.example.moodchon.domain.mood.entity.MoodTag;
import java.util.List;

public record MoodCardResponse(
        Long id,
        String imageUrl,
        String accommodationTypeName,
        List<String> tagNames
) {

    public static MoodCardResponse from(MoodCard card) {
        return new MoodCardResponse(
                card.getId(),
                card.getImageUrl(),
                card.getAccommodationType().getName(),
                card.getTags().stream().map(MoodTag::getName).toList()
        );
    }
}
