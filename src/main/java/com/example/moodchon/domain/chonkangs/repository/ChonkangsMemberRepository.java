package com.example.moodchon.domain.chonkangs.repository;

import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChonkangsMemberRepository extends JpaRepository<ChonkangsMember, Long> {

    long countByChonkangId(Long chonkangId);

    boolean existsByChonkangIdAndUserId(Long chonkangId, Long userId);
}
