package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record UpcomingChonkangResponse(
        Long id,
        String name,
        String moodName,
        String thumbnailUrl,
        LocalDate startDate,
        LocalDate endDate,
        long dDay,
        long memberCount
) {

    public static UpcomingChonkangResponse of(Chonkangs chonkang, LocalDate today, long memberCount) {
        return new UpcomingChonkangResponse(
                chonkang.getId(),
                chonkang.getName(),
                chonkang.getMoodName(),
                chonkang.getThumbnailUrl(),
                chonkang.getStartDate(),
                chonkang.getEndDate(),
                ChronoUnit.DAYS.between(today, chonkang.getStartDate()),
                memberCount
        );
    }
}
