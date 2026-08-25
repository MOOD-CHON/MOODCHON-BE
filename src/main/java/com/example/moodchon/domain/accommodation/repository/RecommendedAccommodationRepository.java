package com.example.moodchon.domain.accommodation.repository;

import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedAccommodationRepository extends JpaRepository<RecommendedAccommodation, Long> {

    List<RecommendedAccommodation> findAllByChonkangIdOrderByRankAsc(Long chonkangId);

    void deleteAllByChonkangId(Long chonkangId);
}
