package com.example.moodchon.domain.place.repository;

import com.example.moodchon.domain.place.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsByPlaceId(Long placeId);

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
