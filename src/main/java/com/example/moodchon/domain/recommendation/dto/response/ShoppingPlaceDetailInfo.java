package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.accommodation.external.TourApiIntroFields;
import java.util.Arrays;
import java.util.List;

// 8.2.4 쇼핑 상세.
public record ShoppingPlaceDetailInfo(
        List<String> saleItems,
        String restDay,
        String businessHours,
        String parkingInfo,
        String contactPhone
) {

    public static ShoppingPlaceDetailInfo of(TourApiIntroFields fields) {
        return new ShoppingPlaceDetailInfo(
                splitSaleItems(fields.saleItem()),
                blank(fields.restDateShopping()),
                blank(fields.openTimeShopping()),
                blank(fields.parkingShopping()),
                blank(fields.infoCenterShopping())
        );
    }

    private static List<String> splitSaleItems(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[,/]"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private static String blank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
