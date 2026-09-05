package com.example.moodchon.domain.recommendation.ai;

import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

// 9.3.1-1 활동 추천 목록, 9.3.3 검색 결과 미리보기에서 장소-무드 적합도를 채점할 때 사용한다.
@Component
@RequiredArgsConstructor
public class ActivityScorePlanner {

    private static final String SCHEMA_NAME = "activity_score";

    private final OpenAiClient openAiClient;
    private final JsonMapper jsonMapper;

    public ActivityScoreResult score(ActivityScoreContext context) {
        String rawJson = openAiClient.completeAsJson(
                ActivityScorePromptBuilder.buildSystemPrompt(),
                ActivityScorePromptBuilder.buildUserPrompt(context),
                ActivityScoreSchema.asJsonSchema(),
                SCHEMA_NAME
        );

        try {
            return jsonMapper.readValue(rawJson, ActivityScoreResult.class);
        } catch (JacksonException e) {
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }
    }
}
