package com.example.moodchon.domain.recommendation.ai;

import java.util.List;
import java.util.Map;

final class ItineraryPlanSchema {

    private ItineraryPlanSchema() {
    }

    static Map<String, Object> asJsonSchema() {
        Map<String, Object> itemSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "placeId", Map.of("type", "integer"),
                        "order", Map.of("type", "integer"),
                        "moodFitScore", Map.of("type", "integer"),
                        "aiSummary", Map.of("type", "string")
                ),
                "required", List.of("placeId", "order", "moodFitScore", "aiSummary"),
                "additionalProperties", false
        );

        Map<String, Object> itemsArraySchema = Map.of(
                "type", "array",
                "items", itemSchema
        );

        Map<String, Object> daySchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "dayNumber", Map.of("type", "integer"),
                        "items", itemsArraySchema
                ),
                "required", List.of("dayNumber", "items"),
                "additionalProperties", false
        );

        Map<String, Object> daysArraySchema = Map.of(
                "type", "array",
                "items", daySchema
        );

        return Map.of(
                "type", "object",
                "properties", Map.of("days", daysArraySchema),
                "required", List.of("days"),
                "additionalProperties", false
        );
    }
}
