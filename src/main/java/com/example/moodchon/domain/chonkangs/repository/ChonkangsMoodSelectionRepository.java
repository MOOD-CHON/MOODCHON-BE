package com.example.moodchon.domain.chonkangs.repository;

import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChonkangsMoodSelectionRepository extends JpaRepository<ChonkangsMoodSelection, Long> {

    List<ChonkangsMoodSelection> findAllByChonkangId(Long chonkangId);

    void deleteAllByChonkangId(Long chonkangId);

    void deleteAllByChonkangIdAndUserId(Long chonkangId, Long userId);
}
