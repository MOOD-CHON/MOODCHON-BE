package com.example.moodchon.domain.place.repository;

import com.example.moodchon.domain.place.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsByPlaceId(Long placeId);

    List<Post> findAllByPlaceIdIn(List<Long> placeIds);

    // 무드 카드 풀. 카드 선택 화면은 게시물의 장소·태그를 함께 보여주므로 한 번에 fetch join 한다.
    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.place LEFT JOIN FETCH p.tags")
    List<Post> findAllWithPlaceAndTags();

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findAllByTagsIdOrderByCreatedAtDesc(Long tagId);

    @Query("""
            SELECT p FROM Post p
            JOIN p.place place
            WHERE LOWER(place.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(place.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY p.createdAt DESC
            """)
    List<Post> searchByPlaceNameOrDescription(@Param("keyword") String keyword);
}
