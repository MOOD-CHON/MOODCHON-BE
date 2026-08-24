package com.example.moodchon.domain.chonkangs.dto.response;

import java.util.List;

public record ChonkangHomeResponse(
        boolean hasOngoingChonkang,
        UpcomingChonkangResponse upcoming,
        List<ChonkangSummaryResponse> records
) {

    public static ChonkangHomeResponse of(UpcomingChonkangResponse upcoming, List<ChonkangSummaryResponse> records) {
        return new ChonkangHomeResponse(upcoming != null, upcoming, records);
    }
}
