package com.example.moodchon.domain.accommodation.ai;

import com.example.moodchon.domain.chonkangs.entity.AccommodationCondition;
import java.util.List;
import java.util.Set;

public record AccommodationMatchContext(
        String moodName,
        String moodDescription,
        int plannedMemberCount,
        Set<AccommodationCondition> accommodationConditions,
        List<AccommodationCandidate> candidates
) {
}
