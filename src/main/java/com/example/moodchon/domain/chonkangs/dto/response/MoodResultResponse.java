package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;

public record MoodResultResponse(
        String name,
        String description
) {

    public static MoodResultResponse of(Chonkangs chonkang) {
        return new MoodResultResponse(chonkang.getMoodName(), chonkang.getMoodDescription());
    }
}
