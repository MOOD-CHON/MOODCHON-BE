package com.example.moodchon.domain.recommendation.ai;

import com.example.moodchon.domain.recommendation.ai.dto.ChatCompletionResponse;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class OpenAiClient {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final JsonMapper jsonMapper;

    public OpenAiClient(OpenAiProperties properties, JsonMapper jsonMapper) {
        this.properties = properties;
        this.jsonMapper = jsonMapper;
        this.restClient = RestClient.create();
    }

    public String completeAsJson(String systemPrompt, String userPrompt, Map<String, Object> jsonSchema,
                                  String schemaName) {
        Map<String, Object> requestBody = Map.of(
                "model", properties.model(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "response_format", Map.of(
                        "type", "json_schema",
                        "json_schema", Map.of(
                                "name", schemaName,
                                "strict", true,
                                "schema", jsonSchema
                        )
                )
        );

        try {
            String rawResponseBody = restClient.post()
                    .uri(CHAT_COMPLETIONS_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonMapper.writeValueAsString(requestBody))
                    .retrieve()
                    .body(String.class);

            ChatCompletionResponse response = jsonMapper.readValue(rawResponseBody, ChatCompletionResponse.class);

            if (response == null || response.choices().isEmpty()) {
                throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
            }

            return response.choices().get(0).message().content();
        } catch (RestClientException | JacksonException e) {
            throw new CustomException(ErrorCode.AI_GENERATION_FAILED);
        }
    }
}
