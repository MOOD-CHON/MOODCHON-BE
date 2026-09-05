package com.example.moodchon.domain.recommendation.dto.request;

import com.example.moodchon.domain.place.entity.PlaceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 9.3.1-2 "장소 없이 추가하기". 9.3.1-3의 필드별 안내 문구는 프론트에서 처리하고, 서버는 공통 검증만 한다.
public record AddCustomItineraryItemRequest(
        @NotBlank String name,
        @NotNull PlaceCategory category
) {
}
