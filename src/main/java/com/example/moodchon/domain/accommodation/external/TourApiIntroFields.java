package com.example.moodchon.domain.accommodation.external;

// TourAPI detailIntro2 응답의 장소 유형별 원본 필드를 그대로 옮겨 담은 값. 카테고리마다 실제로
// 채워지는 필드만 다르고 나머지는 빈 문자열로 내려오며, 유형별 가공(파싱/조합)은 호출하는 도메인이 담당한다.
public record TourApiIntroFields(
        // 관광지 · 문화시설 · 레포츠
        String restDate,
        String useTime,
        String useSeason,
        String parking,
        String parkingFee,
        String infoCenter,
        String accomCount,
        String chkBabyCarriage,
        String chkPet,
        String chkCreditCard,
        String expGuide,
        String expAgeRange,
        // 행사 · 공연 · 축제
        String eventStartDate,
        String eventEndDate,
        String playTime,
        String useTimeFestival,
        String ageLimit,
        String eventPlace,
        String program,
        String sponsor1,
        String sponsor1Tel,
        String eventHomepage,
        // 음식점
        String firstMenu,
        String treatMenu,
        String openTimeFood,
        String restDateFood,
        String packing,
        String infoCenterFood,
        // 쇼핑
        String saleItem,
        String openTimeShopping,
        String restDateShopping,
        String parkingShopping,
        String shopGuide,
        String infoCenterShopping
) {
}
