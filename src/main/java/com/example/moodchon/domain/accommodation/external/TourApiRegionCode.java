package com.example.moodchon.domain.accommodation.external;

import com.example.moodchon.domain.chonkangs.entity.Region;
import java.util.Map;

// TourAPI 4.0 표준 지역 코드(areaCode). https://www.data.go.kr 공공데이터포털 문서 기준
final class TourApiRegionCode {

    private static final Map<Region, String> AREA_CODES = Map.ofEntries(
            Map.entry(Region.SEOUL, "1"),
            Map.entry(Region.INCHEON, "2"),
            Map.entry(Region.DAEJEON, "3"),
            Map.entry(Region.DAEGU, "4"),
            Map.entry(Region.GWANGJU, "5"),
            Map.entry(Region.BUSAN, "6"),
            Map.entry(Region.ULSAN, "7"),
            Map.entry(Region.SEJONG, "8"),
            Map.entry(Region.GYEONGGI, "31"),
            Map.entry(Region.GANGWON, "32"),
            Map.entry(Region.CHUNGCHEONGBUK, "33"),
            Map.entry(Region.CHUNGCHEONGNAM, "34"),
            Map.entry(Region.JEONBUK, "35"),
            Map.entry(Region.JEOLLANAM, "36"),
            Map.entry(Region.GYEONGSANGBUK, "37"),
            Map.entry(Region.GYEONGSANGNAM, "38"),
            Map.entry(Region.JEJU, "39")
    );

    private TourApiRegionCode() {
    }

    static String resolve(Region region) {
        String areaCode = AREA_CODES.get(region);
        if (areaCode == null) {
            throw new IllegalStateException("TourAPI 지역 코드가 정의되지 않은 지역입니다: " + region);
        }
        return areaCode;
    }
}
