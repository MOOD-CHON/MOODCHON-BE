package com.example.moodchon.domain.accommodation.repository;

import com.example.moodchon.domain.accommodation.entity.AccommodationVote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccommodationVoteRepository extends JpaRepository<AccommodationVote, Long> {

    long countByRecommendedAccommodationId(Long recommendedAccommodationId);

    boolean existsByRecommendedAccommodationIdAndUserId(Long recommendedAccommodationId, Long userId);

    void deleteAllByRecommendedAccommodationIdIn(List<Long> recommendedAccommodationIds);

    @Query("""
            SELECT v FROM AccommodationVote v
            JOIN FETCH v.user
            WHERE v.recommendedAccommodation.id = :recommendedAccommodationId
            """)
    List<AccommodationVote> findAllWithUserByRecommendedAccommodationId(
            @Param("recommendedAccommodationId") Long recommendedAccommodationId);
}
