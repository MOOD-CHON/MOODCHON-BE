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

    // accommodationConditions는 지연 로딩 컬렉션이라, 트랜잭션이 끝난 뒤(응답 직렬화 시점)
    // 접근하면 LazyInitializationException이 난다. 여기서 새 HashSet으로 복사해
    // 트랜잭션이 열려있는 이 시점에 강제로 초기화해둔다.
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
                new HashSet<>(chonkang.getAccommodationConditions()),
                currentMemberCount,
                chonkang.isMoodDecided()
        );
    }
}
