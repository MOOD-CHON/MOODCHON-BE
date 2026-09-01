package com.example.moodchon.domain.save.dto.request;

import com.example.moodchon.domain.save.entity.SaveFolder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSaveFolderRequest(
        @NotBlank @Size(max = SaveFolder.MAX_NAME_LENGTH) String name
) {
}
