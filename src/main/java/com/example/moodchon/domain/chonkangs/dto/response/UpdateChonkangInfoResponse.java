package com.example.moodchon.domain.chonkangs.dto.response;

public record UpdateChonkangInfoResponse(
        boolean needsMoodReselect,
        boolean accommodationResetRequired
) {
}
