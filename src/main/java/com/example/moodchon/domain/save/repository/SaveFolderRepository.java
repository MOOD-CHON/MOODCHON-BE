package com.example.moodchon.domain.save.repository;

import com.example.moodchon.domain.save.entity.SaveFolder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaveFolderRepository extends JpaRepository<SaveFolder, Long> {

    List<SaveFolder> findAllByUserId(Long userId);
}
