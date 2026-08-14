package com.example.moodchon.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class CookieAuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                          HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(response);
            return;
        }
        addCookie(response, serialize(authorizationRequest));
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                   HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(response);
        return authorizationRequest;
    }

    private Optional<Cookie> getCookie(HttpServletRequest request) {
        return Optional.ofNullable(WebUtils.getCookie(request, COOKIE_NAME));
    }

    private void addCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // 쿠키는 클라이언트가 임의로 조작해 보낼 수 있어서, Java 네이티브 직렬화(SerializationUtils) 대신
    // 역직렬화 공격 위험이 없는 JSON(Map<String,Object>)으로 필요한 필드만 담아 직렬화한다.
    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            Map<String, Object> fields = new HashMap<>();
            fields.put("authorizationUri", authorizationRequest.getAuthorizationUri());
            fields.put("clientId", authorizationRequest.getClientId());
            fields.put("redirectUri", authorizationRequest.getRedirectUri());
            fields.put("scopes", authorizationRequest.getScopes());
            fields.put("state", authorizationRequest.getState());
            fields.put("additionalParameters", authorizationRequest.getAdditionalParameters());
            fields.put("attributes", authorizationRequest.getAttributes());
            String json = objectMapper.writeValueAsString(fields);
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("OAuth2AuthorizationRequest 직렬화에 실패했습니다.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(cookie.getValue()), StandardCharsets.UTF_8);
            Map<String, Object> fields = objectMapper.readValue(json, new TypeReference<>() {
            });

            List<String> scopeList = (List<String>) fields.get("scopes");
            Set<String> scopes = scopeList != null ? new HashSet<>(scopeList) : Set.of();

            return OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri((String) fields.get("authorizationUri"))
                    .clientId((String) fields.get("clientId"))
                    .redirectUri((String) fields.get("redirectUri"))
                    .scopes(scopes)
                    .state((String) fields.get("state"))
                    .additionalParameters((Map<String, Object>) fields.get("additionalParameters"))
                    .attributes((Map<String, Object>) fields.get("attributes"))
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}