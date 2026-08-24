package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class TripDateValidator {

    private static final long MAX_TRIP_SPAN_DAYS = 6;

    public void validate(LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }
        long spanDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (spanDays < 1 || spanDays > MAX_TRIP_SPAN_DAYS) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
}
