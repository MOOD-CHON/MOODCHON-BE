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

    // 숙소 추천 후보. 이미 수집·태깅된 숙소 중에서 무드의 핵심 태그와 많이 겹치는 순으로 뽑는다.
    // 대표 이미지가 없는 곳은 제외하고, addressPrefix가 있으면 해당 지역만 본다(없으면 전국).
    // 태그가 하나도 안 겹쳐도 후보에는 남긴다 - 태그만으로 자르면 설명상 잘 맞는 숙소를 놓친다.
    @Query(value = """
            SELECT pl.id AS placeId,
                   COUNT(DISTINCT CASE WHEN pt.mood_tag_id IN (:coreTagIds) THEN pt.mood_tag_id END) AS tagOverlap
            FROM places pl
            JOIN posts p ON p.place_id = pl.id
            JOIN post_tags pt ON pt.post_id = p.id
            WHERE pl.category = 'ACCOMMODATION'
              AND COALESCE(pl.thumbnail_url, '') <> ''
              AND (:addressPrefix IS NULL OR pl.address LIKE CONCAT(:addressPrefix, '%'))
            GROUP BY pl.id
            ORDER BY tagOverlap DESC, pl.id
            LIMIT :limit
            """, nativeQuery = true)
    List<AccommodationCandidateRow> findAccommodationCandidates(@Param("coreTagIds") List<Long> coreTagIds,
                                                                 @Param("addressPrefix") String addressPrefix,
                                                                 @Param("limit") int limit);

    @Query("""
            SELECT p FROM Place p
            WHERE p.latitude BETWEEN :minLat AND :maxLat
            AND p.longitude BETWEEN :minLng AND :maxLng
            """)
    List<Place> findNearby(@Param("minLat") double minLat, @Param("maxLat") double maxLat,
                            @Param("minLng") double minLng, @Param("maxLng") double maxLng,
                            Pageable pageable);
}
