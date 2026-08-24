package com.example.moodchon.domain.mood.repository;

import com.example.moodchon.domain.mood.entity.MoodTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoodTagRepository extends JpaRepository<MoodTag, Long> {
}
