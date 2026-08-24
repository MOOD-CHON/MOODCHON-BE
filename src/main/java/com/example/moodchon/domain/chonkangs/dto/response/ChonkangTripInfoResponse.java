package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.AccommodationCondition;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.CompanionType;
import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.chonkangs.entity.TravelMethod;
import java.time.LocalDate;
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
        long currentMemberCount
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
                chonkang.getAccommodationConditions(),
                currentMemberCount
        );
    }
}
