package com.example.moodchon.domain.accommodation.external;

// TourAPI detailIntro2 응답 중 숙소(contentTypeId=32) 전용 원본 필드.
// 숙소는 관광지/음식점/쇼핑과 필드 구성이 완전히 달라 TourApiIntroFields와 별도로 둔다.
// 실제 응답 확인 결과 반려동물 동반 관련 필드는 존재하지 않는다.
public record TourApiLodgingIntroFields(
        String checkInTime,
        String checkOutTime,
        String chkCooking,
        String barbecue,
        String accomCountLodging
) {
}
