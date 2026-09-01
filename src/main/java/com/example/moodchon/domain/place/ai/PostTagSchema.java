package com.example.moodchon.domain.place.ai;

import java.util.List;
import java.util.Map;

final class PostTagSchema {

    private PostTagSchema() {
    }

    static Map<String, Object> asJsonSchema() {
        Map<String, Object> tagNamesArraySchema = Map.of(
                "type", "array",
                "items", Map.of("type", "string")
        );

        return Map.of(
                "type", "object",
                "properties", Map.of("tagNames", tagNamesArraySchema),
                "required", List.of("tagNames"),
                "additionalProperties", false
        );
    }
}
