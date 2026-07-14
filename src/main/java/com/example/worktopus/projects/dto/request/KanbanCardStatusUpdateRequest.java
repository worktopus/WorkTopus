package com.example.worktopus.projects.dto.request;

import com.example.worktopus.projects.entity.KanbanStatus;
import jakarta.validation.constraints.NotNull;

public record KanbanCardStatusUpdateRequest(

        @NotNull
        KanbanStatus status
) {
}
