package com.example.moodchon.auth.repository;

import com.example.moodchon.auth.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
