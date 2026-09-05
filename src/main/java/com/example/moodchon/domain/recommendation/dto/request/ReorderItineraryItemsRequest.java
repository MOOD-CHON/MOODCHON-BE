package com.example.moodchon.domain.recommendation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

// 9.2 저장하기. itemIds는 해당 일차 항목 전체를 새로운 순서로 나열한 목록이다.
public record ReorderItineraryItemsRequest(
        @NotEmpty List<Long> itemIds
) {
}
