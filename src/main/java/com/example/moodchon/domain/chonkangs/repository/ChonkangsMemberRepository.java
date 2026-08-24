package com.example.moodchon.domain.chonkangs.repository;

import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChonkangsMemberRepository extends JpaRepository<ChonkangsMember, Long> {

    long countByChonkangId(Long chonkangId);

    boolean existsByChonkangIdAndUserId(Long chonkangId, Long userId);

    void deleteByChonkangIdAndUserId(Long chonkangId, Long userId);

    @Query("SELECT cm FROM ChonkangsMember cm JOIN FETCH cm.user WHERE cm.chonkang.id = :chonkangId")
    List<ChonkangsMember> findAllWithUserByChonkangId(@Param("chonkangId") Long chonkangId);
}
