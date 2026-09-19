package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.accommodation.dto.response.AccommodationVoterResponse;
import com.example.moodchon.domain.accommodation.dto.response.RecommendedAccommodationRoomResponse;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.place.entity.Place;
import java.util.ArrayList;
import java.util.List;

// 8.3.1/8.3.2 확정된 숙소 상세 + 확정 취소 화면.
// 필드 구성을 RecommendedAccommodationResponse와 맞춰서 클라이언트가 같은 파서를 쓸 수 있게 한다.
// matchScore/rank/좋은 점/아쉬운 점/편의시설/객실은 추천 목록을 거쳐 확정된 경우에만 채워지고,
// "직접 찾은 숙소"를 바로 확정한 경우에는 null/0/빈 리스트로 내려간다.
public record ConfirmedAccommodationDetailResponse(
        Long placeId,
        String name,
        String address,
        String thumbnailUrl,
        String description,
        List<String> images,
        List<String> tags,
        Integer matchScore,
        Integer rank,
        List<String> highlights,
        List<String> regrets,
        Boolean barbecueAvailable,
        Boolean cookingAvailable,
        Boolean petFriendly,
        Boolean bicycleAvailable,
        Boolean campfireAvailable,
        Boolean parkingAvailable,
        Boolean saunaAvailable,
        Boolean sportsAvailable,
        String checkInTime,
        String checkOutTime,
        String contact,
        String reservationUrl,
        List<RecommendedAccommodationRoomResponse> rooms,
        long voteCount,
        boolean votedByMe,
        List<AccommodationVoterResponse> voters
) {

    public static ConfirmedAccommodationDetailResponse of(Place place, List<String> images, List<String> tags,
                                                           RecommendedAccommodation recommended, long voteCount,
                                                           boolean votedByMe,
                                                           List<AccommodationVoterResponse> voters) {
        return new ConfirmedAccommodationDetailResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                resolveThumbnailUrl(place.getThumbnailUrl(), images),
                place.getDescription(),
                images,
                tags,
                recommended == null ? null : recommended.getMatchScore(),
                recommended == null ? null : recommended.getRank(),
                recommended == null ? List.of() : new ArrayList<>(recommended.getHighlights()),
                recommended == null ? List.of() : new ArrayList<>(recommended.getRegrets()),
                recommended == null ? null : recommended.getBarbecueAvailable(),
                recommended == null ? null : recommended.getCookingAvailable(),
                recommended == null ? null : recommended.getPetFriendly(),
                recommended == null ? null : recommended.getBicycleAvailable(),
                recommended == null ? null : recommended.getCampfireAvailable(),
                recommended == null ? null : recommended.getParkingAvailable(),
                recommended == null ? null : recommended.getSaunaAvailable(),
                recommended == null ? null : recommended.getSportsAvailable(),
                recommended == null ? null : recommended.getCheckInTime(),
                recommended == null ? null : recommended.getCheckOutTime(),
                recommended == null ? null : recommended.getContact(),
                recommended == null ? null : recommended.getReservationUrl(),
                recommended == null ? List.of() : recommended.getRooms().stream()
                        .map(RecommendedAccommodationRoomResponse::from)
                        .toList(),
                voteCount,
                votedByMe,
                voters
        );
    }

    // TourAPI 대표 이미지가 없는 숙소가 많아, 없으면 상세 이미지 중 첫 장으로 대체한다.
    private static String resolveThumbnailUrl(String thumbnailUrl, List<String> images) {
        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            return thumbnailUrl;
        }
        return images.isEmpty() ? null : images.get(0);
    }
}
