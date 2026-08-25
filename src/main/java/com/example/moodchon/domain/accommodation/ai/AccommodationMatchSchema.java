package com.example.moodchon.domain.accommodation.ai;

import java.util.List;
import java.util.Map;

final class AccommodationMatchSchema {

    private AccommodationMatchSchema() {
    }

    static Map<String, Object> asJsonSchema() {
        Map<String, Object> stringArraySchema = Map.of(
                "type", "array",
                "items", Map.of("type", "string")
        );

        Map<String, Object> accommodationSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "placeId", Map.of("type", "integer"),
                        "matchScore", Map.of("type", "integer"),
                        "tags", stringArraySchema,
                        "highlights", stringArraySchema
                ),
                "required", List.of("placeId", "matchScore", "tags", "highlights"),
                "additionalProperties", false
        );

        Map<String, Object> accommodationsArraySchema = Map.of(
                "type", "array",
                "items", accommodationSchema
        );

        return Map.of(
                "type", "object",
                "properties", Map.of("accommodations", accommodationsArraySchema),
                "required", List.of("accommodations"),
                "additionalProperties", false
        );
    }
}
