package com.example.moodchon.domain.save.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateSaveFolderSelectionRequest(
        @NotNull List<Long> folderIds
) {
}
