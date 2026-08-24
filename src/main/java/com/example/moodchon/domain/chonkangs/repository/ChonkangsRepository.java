package com.example.moodchon.domain.chonkangs.repository;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChonkangsRepository extends JpaRepository<Chonkangs, Long> {

    @Query("""
            SELECT c FROM Chonkangs c
            JOIN ChonkangsMember cm ON cm.chonkang = c
            WHERE cm.user.id = :userId
            ORDER BY c.startDate DESC
            """)
    List<Chonkangs> findAllByMemberUserId(@Param("userId") Long userId);

    boolean existsByInviteCode(String inviteCode);
}
