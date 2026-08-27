package com.example.moodchon.domain.accommodation.ai;

import com.example.moodchon.domain.recommendation.ai.OpenAiClient;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class AccommodationMatcher {

    private static final String SCHEMA_NAME = "accommodation_match";

    private final OpenAiClient openAiClient;
    private final JsonMapper jsonMapper;

    public AccommodationMatchResult match(AccommodationMatchContext context) {
        String rawJson = openAiClient.completeAsJson(
                AccommodationPromptBuilder.buildSystemPrompt(),
                AccommodationPromptBuilder.buildUserPrompt(context),
                AccommodationMatchSchema.asJsonSchema(),
                SCHEMA_NAME
        );

        try {
            return jsonMapper.readValue(rawJson, AccommodationMatchResult.class);
        } catch (JacksonException e) {
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }
    }
}
