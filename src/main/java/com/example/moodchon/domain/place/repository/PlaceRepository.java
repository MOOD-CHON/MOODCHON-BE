package com.example.moodchon.domain.place.repository;

import com.example.moodchon.domain.place.entity.Place;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByExternalContentId(String externalContentId);

    @Query("""
            SELECT p FROM Place p
            WHERE p.latitude BETWEEN :minLat AND :maxLat
            AND p.longitude BETWEEN :minLng AND :maxLng
            """)
    List<Place> findNearby(@Param("minLat") double minLat, @Param("maxLat") double maxLat,
                            @Param("minLng") double minLng, @Param("maxLng") double maxLng,
                            Pageable pageable);
}
