package com.example.moodchon.global.config;

import com.example.moodchon.auth.apple.AppleAuthorizationRequestResolver;
import com.example.moodchon.auth.apple.AppleClientRegistrationRepository;
import com.example.moodchon.auth.apple.AppleClientSecretGenerator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

// 이 클래스가 ClientRegistrationRepository 빈을 직접 정의하면 Boot의 기본
// ClientRegistrationRepositoryConfiguration(@ConditionalOnMissingBean)이 통째로 스킵되어
// 그 안에 있던 @EnableConfigurationProperties(OAuth2ClientProperties.class)도 같이 사라진다.
// 그래서 OAuth2ClientProperties 바인딩을 여기서 직접 활성화해준다.
@Configuration
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class OAuth2ClientConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            OAuth2ClientProperties properties,
            AppleClientSecretGenerator appleClientSecretGenerator) {
        List<ClientRegistration> registrations =
                new ArrayList<>(new OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values());

        return new AppleClientRegistrationRepository(
                new InMemoryClientRegistrationRepository(registrations), appleClientSecretGenerator);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new AppleAuthorizationRequestResolver(clientRegistrationRepository);
    }
}