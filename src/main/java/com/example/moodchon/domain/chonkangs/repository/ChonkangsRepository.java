package com.example.moodchon.domain.chonkangs.repository;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    Optional<Chonkangs> findByInviteCode(String inviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Chonkangs c WHERE c.inviteCode = :inviteCode")
    Optional<Chonkangs> findByInviteCodeForUpdate(@Param("inviteCode") String inviteCode);
}
