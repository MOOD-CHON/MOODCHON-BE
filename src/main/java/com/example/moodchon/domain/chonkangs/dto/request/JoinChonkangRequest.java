package com.example.moodchon.domain.chonkangs.dto.request;

import jakarta.validation.constraints.Size;
import java.util.Set;

public record JoinChonkangRequest(
        @Size(min = 3, max = 3) Set<Long> selectedMoodCardIds
) {
}
