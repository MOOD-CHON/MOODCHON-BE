package com.example.moodchon.auth;

import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

// 웹 로그인 전용. 브라우저는 인가 코드까지만 받을 수 있어(카카오 SDK가 웹에서 토큰 발급을 막아둠)
// 코드를 액세스 토큰으로 바꾸는 일은 서버가 한다. 네이티브 앱은 SDK가 토큰까지 발급하므로 이 경로를 타지 않는다.
//
// client_id는 REST API 키가 아니라 JavaScript 키다. 카카오 Flutter SDK가 웹에서
// appKey = JavaScript 키로 인가 코드를 받아오기 때문에(KakaoSdk.appKey => kIsWeb ? jsKey : nativeKey),
// 토큰 교환도 같은 키로 해야 한다. REST API 키를 쓰면 KOE010(Bad client credentials)이 난다.
@Slf4j
@Component
public class KakaoTokenExchanger {

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    private final RestClient restClient = RestClient.create();
    private final String javaScriptKey;
    private final String clientSecret;

    public KakaoTokenExchanger(@Value("${kakao.javascript-key:}") String javaScriptKey,
                                @Value("${kakao.client-secret:}") String clientSecret) {
        this.javaScriptKey = javaScriptKey;
        this.clientSecret = clientSecret;
    }

    @SuppressWarnings("unchecked")
    public String exchangeForAccessToken(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", javaScriptKey);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        // 카카오 디벨로퍼스에서 Client Secret을 "사용함"으로 둔 경우에만 필요하다.
        if (StringUtils.hasText(clientSecret)) {
            form.add("client_secret", clientSecret);
        }

        try {
            Map<String, Object> response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            Object accessToken = response == null ? null : response.get("access_token");
            if (accessToken == null) {
                log.warn("[카카오 토큰 교환] access_token 없음 | response={}", response);
                throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
            }
            return accessToken.toString();
        } catch (RestClientResponseException e) {
            // 카카오가 내려주는 error/error_code가 원인 파악의 핵심이라 그대로 남긴다.
            log.warn("[카카오 토큰 교환] 실패 | status={} redirectUri={} clientIdSet={} clientSecretSet={} body={}",
                    e.getStatusCode(), redirectUri, StringUtils.hasText(javaScriptKey),
                    StringUtils.hasText(clientSecret), e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        } catch (RestClientException e) {
            log.warn("[카카오 토큰 교환] 호출 실패 | redirectUri={} error={}", redirectUri, e.toString());
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}
