package com.example.moodchon.domain.accommodation.external;

import com.example.moodchon.domain.place.entity.PlaceCategory;
import java.util.Map;
import java.util.Set;

// TourAPI 4.0 콘텐츠 타입(contentTypeId). EVENT/PERFORMANCE/FESTIVAL은 TourAPI에서 15(축제공연행사)로
// 통합되어 있어 이번 동기화에서는 FESTIVAL로만 매핑한다 (EVENT/PERFORMANCE는 이 방식으로 채우지 않음).
final class TourApiCategoryCode {

    private static final Map<PlaceCategory, String> CONTENT_TYPE_IDS = Map.of(
            PlaceCategory.TOURIST_SPOT, "12",
            PlaceCategory.CULTURAL_FACILITY, "14",
            PlaceCategory.FESTIVAL, "15",
            PlaceCategory.LEISURE_SPORTS, "28",
            PlaceCategory.SHOPPING, "38",
            PlaceCategory.RESTAURANT, "39",
            PlaceCategory.ACCOMMODATION, "32"
    );

    private TourApiCategoryCode() {
    }

    static String resolve(PlaceCategory category) {
        String contentTypeId = CONTENT_TYPE_IDS.get(category);
        if (contentTypeId == null) {
            throw new IllegalArgumentException("TourAPI로 동기화할 수 없는 장소 카테고리입니다: " + category);
        }
        return contentTypeId;
    }

    static Set<PlaceCategory> syncableCategories() {
        return CONTENT_TYPE_IDS.keySet();
    }
}
