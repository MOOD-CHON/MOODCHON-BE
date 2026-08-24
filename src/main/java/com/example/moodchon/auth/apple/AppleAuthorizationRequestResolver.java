package com.example.moodchon.auth.apple;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 애플은 scope에 name/email이 포함되면 인가 요청에 response_mode=form_post가
 * 명시적으로 없으면 실패한다. Spring Security의 additionalParameters는 이미
 * 계산된 authorizationRequestUri에 반영되지 않을 수 있어, URI 문자열에 직접 붙인다.
 */
public class AppleAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String APPLE_REGISTRATION_ID = "apple";
    private static final String AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public AppleAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, AUTHORIZATION_REQUEST_BASE_URI);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return withFormPostForApple(delegate.resolve(request));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        return withFormPostForApple(delegate.resolve(request, clientRegistrationId));
    }

    private OAuth2AuthorizationRequest withFormPostForApple(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null || !isAppleRequest(authorizationRequest)) {
            return authorizationRequest;
        }

        String authorizationRequestUri = UriComponentsBuilder
                .fromUriString(authorizationRequest.getAuthorizationRequestUri())
                .queryParam("response_mode", "form_post")
                .build()
                .toUriString();

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .authorizationRequestUri(authorizationRequestUri)
                .build();
    }

    private boolean isAppleRequest(OAuth2AuthorizationRequest authorizationRequest) {
        return APPLE_REGISTRATION_ID.equals(
                authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID));
    }
}