package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.place.entity.Place;
import java.util.ArrayList;
import java.util.List;

// 여행방 메인의 확정 숙소 카드. highlights/tags는 추천 목록을 거쳐 확정된 경우에만 채워지고,
// "직접 찾은 숙소"를 바로 확정한 경우에는 빈 리스트로 내려간다.
public record ConfirmedAccommodationResponse(
        Long placeId,
        String name,
        String address,
        String thumbnailUrl,
        List<String> tags,
        List<String> highlights
) {

    public static ConfirmedAccommodationResponse of(Place place, RecommendedAccommodation recommended) {
        return new ConfirmedAccommodationResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getThumbnailUrl(),
                recommended == null ? List.of() : new ArrayList<>(recommended.getTags()),
                recommended == null ? List.of() : new ArrayList<>(recommended.getHighlights())
        );
    }
}
