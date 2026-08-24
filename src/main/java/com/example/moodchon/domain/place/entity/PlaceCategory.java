package com.example.moodchon.domain.place.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceCategory {

    TOURIST_SPOT("관광지", TagColor.BLUE),
    CULTURAL_FACILITY("문화시설", TagColor.BLUE),
    SHOPPING("쇼핑", TagColor.BLUE),
    LEISURE_SPORTS("레포츠", TagColor.RED),
    EVENT("행사", TagColor.RED),
    PERFORMANCE("공연", TagColor.RED),
    FESTIVAL("축제", TagColor.RED),
    RESTAURANT("음식점", TagColor.PURPLE),
    ACCOMMODATION("숙소", TagColor.GREEN);

    private final String label;
    private final TagColor tagColor;
}
