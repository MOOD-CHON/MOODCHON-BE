package com.example.moodchon.auth.apple;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple")
public record AppleOAuthProperties(
        String teamId,
        String keyId,
        String clientId,
        String privateKey
) {
}