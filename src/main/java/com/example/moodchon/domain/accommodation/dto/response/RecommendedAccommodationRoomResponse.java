package com.example.moodchon.domain.accommodation.dto.response;

import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodationRoom;
import java.util.ArrayList;
import java.util.List;

// 객실 정보. 등록되지 않은 항목은 null이고, 화면에서 해당 줄을 숨긴다.
// 요금은 원 단위 정수로 내려주고 표기 형식(예: "150,000원~")은 클라이언트가 만든다.
public record RecommendedAccommodationRoomResponse(
        Long id,
        String name,
        String imageUrl,
        Integer roomCount,
        Integer baseCount,
        Integer maxCount,
        Integer offSeasonWeekdayFee,
        Integer offSeasonWeekendFee,
        Integer peakSeasonWeekdayFee,
        Integer peakSeasonWeekendFee,
        List<String> facilities
) {

    public static RecommendedAccommodationRoomResponse from(RecommendedAccommodationRoom room) {
        return new RecommendedAccommodationRoomResponse(
                room.getId(),
                room.getName(),
                room.getImageUrl(),
                room.getRoomCount(),
                room.getBaseCount(),
                room.getMaxCount(),
                room.getOffSeasonWeekdayFee(),
                room.getOffSeasonWeekendFee(),
                room.getPeakSeasonWeekdayFee(),
                room.getPeakSeasonWeekendFee(),
                new ArrayList<>(room.getFacilities())
        );
    }
}
