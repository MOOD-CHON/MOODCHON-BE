package com.example.moodchon.domain.mood.repository;

import com.example.moodchon.domain.mood.entity.MoodCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MoodCardRepository extends JpaRepository<MoodCard, Long> {

    @Query("SELECT DISTINCT c FROM MoodCard c JOIN FETCH c.accommodationType LEFT JOIN FETCH c.tags")
    List<MoodCard> findAllWithTagsAndAccommodationType();
}
