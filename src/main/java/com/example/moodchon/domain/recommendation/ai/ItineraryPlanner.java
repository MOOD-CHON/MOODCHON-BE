package com.example.moodchon.domain.recommendation.ai;

import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class ItineraryPlanner {

    private static final String SCHEMA_NAME = "itinerary_plan";

    private final OpenAiClient openAiClient;
    private final JsonMapper jsonMapper;

    public ItineraryPlanResult plan(ItineraryPlanContext context) {
        String rawJson = openAiClient.completeAsJson(
                ItineraryPromptBuilder.buildSystemPrompt(),
                ItineraryPromptBuilder.buildUserPrompt(context),
                ItineraryPlanSchema.asJsonSchema(),
                SCHEMA_NAME
        );

        try {
            return jsonMapper.readValue(rawJson, ItineraryPlanResult.class);
        } catch (JacksonException e) {
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }
    }
}
