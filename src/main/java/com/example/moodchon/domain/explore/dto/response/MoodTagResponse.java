package com.example.moodchon.domain.explore.dto.response;

import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.entity.MoodTagCategory;

public record MoodTagResponse(
        Long id,
        String name,
        MoodTagCategory category
) {

    public static MoodTagResponse of(MoodTag tag) {
        return new MoodTagResponse(tag.getId(), tag.getName(), tag.getCategory());
    }
}
