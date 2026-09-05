package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.accommodation.external.TourApiIntroFields;
import java.util.Arrays;
import java.util.List;

// 8.2.2 행사 · 공연 · 축제 상세.
public record EventPlaceDetailInfo(
        List<String> programs,
        String eventPeriod,
        String showTime,
        String usageFee,
        String ageLimit,
        String eventVenue,
        String organizerInfo,
        String organizerContact,
        String homepage
) {

    public static EventPlaceDetailInfo of(TourApiIntroFields fields) {
        return new EventPlaceDetailInfo(
                splitProgram(fields.program()),
                eventPeriod(fields.eventStartDate(), fields.eventEndDate()),
                blank(fields.playTime()),
                blank(fields.useTimeFestival()),
                blank(fields.ageLimit()),
                blank(fields.eventPlace()),
                blank(fields.sponsor1()),
                blank(fields.sponsor1Tel()),
                blank(fields.eventHomepage())
        );
    }

    private static String eventPeriod(String startDate, String endDate) {
        if (blank(startDate) == null && blank(endDate) == null) {
            return null;
        }
        return "%s ~ %s".formatted(blank(startDate) != null ? startDate : "", blank(endDate) != null ? endDate : "");
    }

    private static List<String> splitProgram(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("(?i)<br\\s*/?>"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .toList();
    }

    private static String blank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
