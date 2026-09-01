package com.example.moodchon.domain.place.repository;

import com.example.moodchon.domain.place.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsByPlaceId(Long placeId);

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findAllByTagsIdOrderByCreatedAtDesc(Long tagId);
}
