package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.chonkangs.entity.Region;
import java.util.Map;

// TourAPI 주소 표기는 "제주특별자치도 서귀포시 ..."처럼 시작한다. 표기가 바뀌는 경우가 있어
// (예: 강원도 -> 강원특별자치도) 뒤가 잘린 짧은 접두사로 느슨하게 매칭한다.
final class RegionAddressPrefix {

    private static final Map<Region, String> PREFIXES = Map.ofEntries(
            Map.entry(Region.SEOUL, "서울"),
            Map.entry(Region.INCHEON, "인천"),
            Map.entry(Region.DAEJEON, "대전"),
            Map.entry(Region.DAEGU, "대구"),
            Map.entry(Region.GWANGJU, "광주"),
            Map.entry(Region.BUSAN, "부산"),
            Map.entry(Region.ULSAN, "울산"),
            Map.entry(Region.SEJONG, "세종"),
            Map.entry(Region.GYEONGGI, "경기"),
            Map.entry(Region.GANGWON, "강원"),
            Map.entry(Region.CHUNGCHEONGBUK, "충청북"),
            Map.entry(Region.CHUNGCHEONGNAM, "충청남"),
            Map.entry(Region.JEONBUK, "전북"),
            Map.entry(Region.JEOLLANAM, "전라남"),
            Map.entry(Region.GYEONGSANGBUK, "경상북"),
            Map.entry(Region.GYEONGSANGNAM, "경상남"),
            Map.entry(Region.JEJU, "제주")
    );

    private RegionAddressPrefix() {
    }

    // 희망 지역이 없으면 null - 전국이 후보가 된다.
    static String resolve(Region region) {
        return region == null ? null : PREFIXES.get(region);
    }
}
