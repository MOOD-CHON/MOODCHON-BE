package com.example.moodchon.domain.chonkangs.dto.request;

import com.example.moodchon.domain.chonkangs.entity.AccommodationCondition;
import com.example.moodchon.domain.chonkangs.entity.CompanionType;
import com.example.moodchon.domain.chonkangs.entity.Region;
import com.example.moodchon.domain.chonkangs.entity.TravelMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public record CreateChonkangRequest(
        @NotBlank @Size(max = 20) String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @Min(1) @Max(6) int plannedMemberCount,
        @NotNull CompanionType companionType,
        @NotNull TravelMethod travelMethod,
        @NotNull Region desiredRegion,
        Set<AccommodationCondition> accommodationConditions,
        @NotEmpty @Size(min = 3, max = 3) Set<Long> selectedMoodCardIds
) {
}
