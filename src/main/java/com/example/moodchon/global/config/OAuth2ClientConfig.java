package com.example.moodchon.global.config;

import com.example.moodchon.auth.apple.AppleClientRegistrationRepository;
import com.example.moodchon.auth.apple.AppleClientSecretGenerator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
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
}