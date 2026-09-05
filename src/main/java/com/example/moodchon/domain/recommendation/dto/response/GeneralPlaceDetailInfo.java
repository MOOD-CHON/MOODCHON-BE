package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.accommodation.external.TourApiIntroFields;

// 8.2.1 관광지 · 문화시설 · 레포츠 상세.
public record GeneralPlaceDetailInfo(
        String restDay,
        String openPeriod,
        String useTime,
        String capacity,
        String parkingInfo,
        String parkingFee,
        String babyCarriageInfo,
        String petInfo,
        String creditCardInfo,
        String experienceGuide,
        String experienceAgeRange,
        String contactPhone
) {

    public static GeneralPlaceDetailInfo of(TourApiIntroFields fields) {
        return new GeneralPlaceDetailInfo(
                blank(fields.restDate()),
                blank(fields.useSeason()),
                blank(fields.useTime()),
                blank(fields.accomCount()),
                blank(fields.parking()),
                blank(fields.parkingFee()),
                blank(fields.chkBabyCarriage()),
                blank(fields.chkPet()),
                blank(fields.chkCreditCard()),
                blank(fields.expGuide()),
                blank(fields.expAgeRange()),
                blank(fields.infoCenter())
        );
    }

    private static String blank(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
