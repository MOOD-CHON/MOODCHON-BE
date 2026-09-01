package com.example.moodchon.domain.mood.repository;

import com.example.moodchon.domain.mood.entity.MoodType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MoodTypeRepository extends JpaRepository<MoodType, Long> {

    @Query("SELECT DISTINCT mt FROM MoodType mt LEFT JOIN FETCH mt.coreTags")
    List<MoodType> findAllWithCoreTags();
}
