package com.example.moodchon.domain.place.repository;

// 무드 태그가 몇 개 겹치는지와 함께 뽑은 숙소 후보.
public interface AccommodationCandidateRow {

    Long getPlaceId();

    long getTagOverlap();
}
