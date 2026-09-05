package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.accommodation.dto.response.AccommodationVoterResponse;
import com.example.moodchon.domain.place.entity.Place;
import java.util.List;

// 8.3.1/8.3.2 확정된 숙소 상세 + 확정 취소 화면. matchScore/rank/voteCount/voters는 추천 숙소 목록을 거쳐
// 확정된 경우에만 채워지고, "직접 찾은 숙소"를 바로 확정한 경우에는 null/0/빈 리스트로 내려간다.
public record ConfirmedAccommodationDetailResponse(
        Long placeId,
        String name,
        String address,
        String description,
        List<String> images,
        List<String> tags,
        Integer matchScore,
        Integer rank,
        long voteCount,
        List<AccommodationVoterResponse> voters
) {

    public static ConfirmedAccommodationDetailResponse of(Place place, List<String> images, List<String> tags,
                                                           Integer matchScore, Integer rank, long voteCount,
                                                           List<AccommodationVoterResponse> voters) {
        return new ConfirmedAccommodationDetailResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getDescription(),
                images,
                tags,
                matchScore,
                rank,
                voteCount,
                voters
        );
    }
}
