package com.example.moodchon.domain.accommodation.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tour-api")
public record TourApiProperties(
        String serviceKey
) {
}
