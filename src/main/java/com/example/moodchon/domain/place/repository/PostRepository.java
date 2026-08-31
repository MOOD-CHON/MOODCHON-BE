package com.example.moodchon.domain.place.repository;

import com.example.moodchon.domain.place.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
