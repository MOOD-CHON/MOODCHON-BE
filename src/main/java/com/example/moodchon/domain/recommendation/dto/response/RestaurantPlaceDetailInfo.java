package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.accommodation.external.TourApiIntroFields;
import java.util.Arrays;
import java.util.List;

// 8.2.3 음식점 상세.
public record RestaurantPlaceDetailInfo(
        List<String> representativeMenus,
        String restDay,
        String businessHours,
        String packingInfo,
        String contactPhone
) {

    public static RestaurantPlaceDetailInfo of(TourApiIntroFields fields) {
        return new RestaurantPlaceDetailInfo(
                splitMenu(fields.firstMenu()),
                blank(fields.restDateFood()),
                blank(fields.openTimeFood()),
                blank(fields.packing()),
                blank(fields.infoCenterFood())
        );
    }

    private static List<String> splitMenu(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[,/]"))
                .map(String::trim)
                .filter(menu -> !menu.isBlank())
                .toList();
    }

    private static String blank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
