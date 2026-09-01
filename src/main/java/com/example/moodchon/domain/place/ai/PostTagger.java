package com.example.moodchon.domain.place.ai;

import com.example.moodchon.domain.recommendation.ai.OpenAiClient;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class PostTagger {

    private static final String SCHEMA_NAME = "post_tags";

    private final OpenAiClient openAiClient;
    private final JsonMapper jsonMapper;

    public PostTagResult tag(PostTagContext context) {
        String rawJson = openAiClient.completeAsJson(
                PostTagPromptBuilder.buildSystemPrompt(),
                PostTagPromptBuilder.buildUserPrompt(context),
                PostTagSchema.asJsonSchema(),
                SCHEMA_NAME
        );

        try {
            return jsonMapper.readValue(rawJson, PostTagResult.class);
        } catch (JacksonException e) {
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }
    }
}
