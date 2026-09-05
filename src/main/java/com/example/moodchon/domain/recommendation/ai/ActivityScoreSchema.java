package com.example.moodchon.domain.recommendation.ai;

import java.util.List;
import java.util.Map;

final class ActivityScoreSchema {

    private ActivityScoreSchema() {
    }

    static Map<String, Object> asJsonSchema() {
        Map<String, Object> itemSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "placeId", Map.of("type", "integer"),
                        "moodFitScore", Map.of("type", "integer"),
                        "aiSummary", Map.of("type", "string")
                ),
                "required", List.of("placeId", "moodFitScore", "aiSummary"),
                "additionalProperties", false
        );

        Map<String, Object> itemsArraySchema = Map.of(
                "type", "array",
                "items", itemSchema
        );

        return Map.of(
                "type", "object",
                "properties", Map.of("items", itemsArraySchema),
                "required", List.of("items"),
                "additionalProperties", false
        );
    }
}
