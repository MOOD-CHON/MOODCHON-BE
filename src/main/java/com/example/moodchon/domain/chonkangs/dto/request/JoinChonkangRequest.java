package com.example.moodchon.domain.chonkangs.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record JoinChonkangRequest(
        @NotEmpty @Size(min = 3, max = 3) Set<Long> selectedMoodCardIds
) {
}
