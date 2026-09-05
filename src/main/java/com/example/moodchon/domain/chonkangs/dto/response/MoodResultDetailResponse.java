package com.example.moodchon.domain.chonkangs.dto.response;

import java.util.List;

public record MoodResultDetailResponse(
        String name,
        String description,
        List<String> collageImageUrls,
        List<TagFrequency> tagBreakdown,
        String summary
) {

    public record TagFrequency(String tagName, long count) {
    }
}
