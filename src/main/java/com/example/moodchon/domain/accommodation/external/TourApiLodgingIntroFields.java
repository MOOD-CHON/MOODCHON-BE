package com.example.moodchon.domain.accommodation.external;

// TourAPI detailIntro2(숙소 전용 필드) + detailPetTour2(반려동물 동반여행 정보, 별도 엔드포인트) 조합.
// 숙소는 관광지/음식점/쇼핑과 필드 구성이 완전히 달라 TourApiIntroFields와 별도로 둔다.
// petAccompanyType은 등록된 숙소가 적어(표본상 10곳 중 1곳꼴) 대부분 null이다.
public record TourApiLodgingIntroFields(
        String checkInTime,
        String checkOutTime,
        String chkCooking,
        String barbecue,
        String accomCountLodging,
        String petAccompanyType
) {
}
