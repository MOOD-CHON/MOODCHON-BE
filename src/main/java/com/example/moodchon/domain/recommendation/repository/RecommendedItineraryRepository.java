package com.example.moodchon.domain.recommendation.repository;

import com.example.moodchon.domain.recommendation.entity.RecommendedItinerary;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedItineraryRepository extends JpaRepository<RecommendedItinerary, Long> {

    Optional<RecommendedItinerary> findByChonkangId(Long chonkangId);
}
