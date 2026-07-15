package com.example.WorkTopus.projects.dto.request;

import com.example.WorkTopus.projects.entity.KanbanStatus;
import jakarta.validation.constraints.NotNull;

public record KanbanCardStatusUpdateRequest(

        @NotNull
        KanbanStatus status
) {
}
