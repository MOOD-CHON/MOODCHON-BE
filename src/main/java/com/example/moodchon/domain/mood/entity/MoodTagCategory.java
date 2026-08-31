package com.example.moodchon.domain.mood.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MoodTagCategory {

    ACCOMMODATION("숙소"),
    PLACE("장소"),
    SCENE("장면"),
    ATMOSPHERE("분위기");

    private final String label;
}
