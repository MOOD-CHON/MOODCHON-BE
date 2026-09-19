package com.example.moodchon.domain.accommodation.external;

import java.util.List;

// TourAPI detailInfo2(contentTypeId=32) 객실 단위 정보. 숙소 하나에 객실이 0~N개 딸린다.
// 요금은 원 단위 정수로, 등록 안 된 항목은 null이다.
public record TourApiRoom(
        String title,
        Integer roomCount,
        Integer baseCount,
        Integer maxCount,
        Integer offSeasonWeekdayFee,
        Integer offSeasonWeekendFee,
        Integer peakSeasonWeekdayFee,
        Integer peakSeasonWeekendFee,
        String imageUrl,
        List<String> facilities
) {
}
