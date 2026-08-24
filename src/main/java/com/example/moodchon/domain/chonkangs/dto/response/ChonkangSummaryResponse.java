package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsStatus;
import java.time.LocalDate;

public record ChonkangSummaryResponse(
        Long id,
        String name,
        String moodName,
        String thumbnailUrl,
        LocalDate startDate,
        LocalDate endDate,
        long memberCount,
        ChonkangsStatus status
) {

    public static ChonkangSummaryResponse of(Chonkangs chonkang, LocalDate today, long memberCount) {
        return new ChonkangSummaryResponse(
                chonkang.getId(),
                chonkang.getName(),
                chonkang.getMoodName(),
                chonkang.getThumbnailUrl(),
                chonkang.getStartDate(),
                chonkang.getEndDate(),
                memberCount,
                chonkang.resolveStatus(today)
        );
    }
}
