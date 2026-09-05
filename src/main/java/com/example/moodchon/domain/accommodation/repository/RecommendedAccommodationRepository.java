package com.example.moodchon.domain.accommodation.repository;

import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendedAccommodationRepository extends JpaRepository<RecommendedAccommodation, Long> {

    List<RecommendedAccommodation> findAllByChonkangIdOrderByRankAsc(Long chonkangId);

    Optional<RecommendedAccommodation> findFirstByChonkangIdAndPlaceIdOrderByIdDesc(Long chonkangId, Long placeId);

    void deleteAllByChonkangId(Long chonkangId);
}
