package com.example.moodchon.domain.recommendation.service;

import com.example.moodchon.domain.accommodation.external.TourApiIntroFields;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.domain.recommendation.dto.response.EventPlaceDetailInfo;
import com.example.moodchon.domain.recommendation.dto.response.GeneralPlaceDetailInfo;
import com.example.moodchon.domain.recommendation.dto.response.RestaurantPlaceDetailInfo;
import com.example.moodchon.domain.recommendation.dto.response.ShoppingPlaceDetailInfo;
import java.util.Set;

// 8.2.1~8.2.4, 9.3.3에서 공용으로 쓰는 카테고리별 상세 블록 매핑. 실제 카테고리에 해당하는 블록 하나만 채워진다.
final class PlaceCategoryDetailMapper {

    private static final Set<PlaceCategory> GENERAL_CATEGORIES =
            Set.of(PlaceCategory.TOURIST_SPOT, PlaceCategory.CULTURAL_FACILITY, PlaceCategory.LEISURE_SPORTS);
    private static final Set<PlaceCategory> EVENT_CATEGORIES =
            Set.of(PlaceCategory.EVENT, PlaceCategory.PERFORMANCE, PlaceCategory.FESTIVAL);

    private PlaceCategoryDetailMapper() {
    }

    record Blocks(GeneralPlaceDetailInfo generalInfo, EventPlaceDetailInfo eventInfo,
                  RestaurantPlaceDetailInfo restaurantInfo, ShoppingPlaceDetailInfo shoppingInfo) {
    }

    static Blocks map(PlaceCategory category, TourApiIntroFields fields) {
        GeneralPlaceDetailInfo generalInfo = GENERAL_CATEGORIES.contains(category)
                ? GeneralPlaceDetailInfo.of(fields) : null;
        EventPlaceDetailInfo eventInfo = EVENT_CATEGORIES.contains(category)
                ? EventPlaceDetailInfo.of(fields) : null;
        RestaurantPlaceDetailInfo restaurantInfo = category == PlaceCategory.RESTAURANT
                ? RestaurantPlaceDetailInfo.of(fields) : null;
        ShoppingPlaceDetailInfo shoppingInfo = category == PlaceCategory.SHOPPING
                ? ShoppingPlaceDetailInfo.of(fields) : null;
        return new Blocks(generalInfo, eventInfo, restaurantInfo, shoppingInfo);
    }
}
