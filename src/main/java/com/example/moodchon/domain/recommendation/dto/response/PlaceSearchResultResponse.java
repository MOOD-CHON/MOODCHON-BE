package com.example.moodchon.domain.recommendation.dto.response;

import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.TagColor;

// 9.3.2 일정 추가하기 검색 결과 카드 (숙소 전용이 아닌 전 카테고리 대상).
public record PlaceSearchResultResponse(
        Long placeId,
        String name,
        String address,
        String categoryLabel,
        TagColor tagColor,
        String thumbnailUrl
) {

    public static PlaceSearchResultResponse from(Place place) {
        return new PlaceSearchResultResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getCategory().getLabel(),
                place.getCategory().getTagColor(),
                place.getThumbnailUrl()
        );
    }
}
