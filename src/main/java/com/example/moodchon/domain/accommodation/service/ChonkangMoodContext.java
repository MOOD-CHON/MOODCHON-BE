package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.chonkangs.entity.AccommodationCondition;
import com.example.moodchon.domain.chonkangs.entity.Region;
import java.util.Set;

// 추천 생성에 필요한 촌캉스 정보를 트랜잭션 밖으로 복사해 나르는 값.
// accommodationConditions는 지연 로딩이라 트랜잭션 안에서 미리 복사해 둔다.
public record ChonkangMoodContext(
        String moodName,
        String moodDescription,
        int plannedMemberCount,
        Region desiredRegion,
        Set<AccommodationCondition> accommodationConditions
) {
}
