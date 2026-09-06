package com.example.moodchon.auth;

import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// authorizationCode를 애플 refresh_token으로 교환하고(로그인 시), 탈퇴 시 그 토큰을 철회한다.
@Component
public class AppleTokenClient {

    private static final String TOKEN_URI = "https://appleid.apple.com/auth/token";
    private static final String REVOKE_URI = "https://appleid.apple.com/auth/revoke";

    private final RestClient restClient = RestClient.create();
    private final JsonMapper jsonMapper;
    private final AppleClientSecretGenerator clientSecretGenerator;
    private final String bundleId;

    public AppleTokenClient(AppleClientSecretGenerator clientSecretGenerator,
                             JsonMapper jsonMapper,
                             @Value("${apple.bundle-id}") String bundleId) {
        this.clientSecretGenerator = clientSecretGenerator;
        this.jsonMapper = jsonMapper;
        this.bundleId = bundleId;
    }

    public String exchangeAuthorizationCode(String authorizationCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);
        body.add("client_id", bundleId);
        body.add("client_secret", clientSecretGenerator.generate());

        try {
            String rawResponseBody = restClient.post()
                    .uri(TOKEN_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(rawResponseBody);
            return root.path("refresh_token").asString(null);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    public void revoke(String appleRefreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", appleRefreshToken);
        body.add("token_type_hint", "refresh_token");
        body.add("client_id", bundleId);
        body.add("client_secret", clientSecretGenerator.generate());

        try {
            restClient.post()
                    .uri(REVOKE_URI)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}
