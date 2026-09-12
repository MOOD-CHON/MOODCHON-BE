package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.AccommodationCondition;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.CompanionType;
import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.chonkangs.entity.TravelMethod;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public record ChonkangTripInfoResponse(
        Long chonkangId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        int plannedMemberCount,
        CompanionType companionType,
        TravelMethod travelMethod,
        Region desiredRegion,
        Set<AccommodationCondition> accommodationConditions,
        long currentMemberCount,
        boolean moodDecided
) {

    public static ChonkangTripInfoResponse of(Chonkangs chonkang, long currentMemberCount) {
        return new ChonkangTripInfoResponse(
                chonkang.getId(),
                chonkang.getName(),
                chonkang.getStartDate(),
                chonkang.getEndDate(),
                chonkang.getPlannedMemberCount(),
                chonkang.getCompanionType(),
                chonkang.getTravelMethod(),
                chonkang.getDesiredRegion(),
                // open-in-view=false라 트랜잭션 밖(Jackson 직렬화 시점)에서 지연 컬렉션에 접근하면
                // LazyInitializationException이 난다 — 서비스가 아직 세션을 들고 있는 지금 실제 값으로 복사해둔다.
                new HashSet<>(chonkang.getAccommodationConditions()),
                currentMemberCount,
                chonkang.isMoodDecided()
        );
    }
}
