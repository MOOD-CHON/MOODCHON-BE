package com.example.moodchon.domain.chonkangs.dto.response;

public record JoinChonkangResponse(
        Long chonkangId,
        boolean isLastParticipant,
        int plannedMemberCount,
        long currentMemberCount
) {
}
