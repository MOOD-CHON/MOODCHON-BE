package com.example.moodchon.domain.accommodation.dto.response;

import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.place.entity.Place;
import java.util.ArrayList;
import java.util.List;

public record RecommendedAccommodationResponse(
        Long placeId,
        String name,
        String address,
        String thumbnailUrl,
        String description,
        List<String> images,
        int matchScore,
        int rank,
        List<String> tags,
        List<String> highlights,
        // 이 숙소를 고를 때 아쉬울 수 있는 점. highlights(좋은 점)와 대칭.
        List<String> regrets,
        // 편의시설. null은 "정보없음"을 뜻하므로 화면에서 뱃지를 표시하지 않는다.
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
        // TourAPI에 객실별 가격/정원이 없어 "양실 1실" 같은 요약 문자열만 제공한다.
        String roomSummary,
        long voteCount,
        boolean votedByMe,
        List<AccommodationVoterResponse> voters
) {

    public static RecommendedAccommodationResponse of(RecommendedAccommodation recommendedAccommodation,
                                                        List<String> images, long voteCount, boolean votedByMe,
                                                        List<AccommodationVoterResponse> voters) {
        Place place = recommendedAccommodation.getPlace();
        return new RecommendedAccommodationResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                resolveThumbnailUrl(place.getThumbnailUrl(), images),
                place.getDescription(),
                images,
                recommendedAccommodation.getMatchScore(),
                recommendedAccommodation.getRank(),
                new ArrayList<>(recommendedAccommodation.getTags()),
                new ArrayList<>(recommendedAccommodation.getHighlights()),
                new ArrayList<>(recommendedAccommodation.getRegrets()),
                recommendedAccommodation.getBarbecueAvailable(),
                recommendedAccommodation.getCookingAvailable(),
                recommendedAccommodation.getPetFriendly(),
                recommendedAccommodation.getBicycleAvailable(),
                recommendedAccommodation.getCampfireAvailable(),
                recommendedAccommodation.getParkingAvailable(),
                recommendedAccommodation.getSaunaAvailable(),
                recommendedAccommodation.getSportsAvailable(),
                recommendedAccommodation.getCheckInTime(),
                recommendedAccommodation.getCheckOutTime(),
                recommendedAccommodation.getContact(),
                recommendedAccommodation.getReservationUrl(),
                recommendedAccommodation.getRoomSummary(),
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
