package com.example.WorkTopus.projects.dto.request;

import com.example.WorkTopus.projects.entity.KanbanPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record KanbanCardCreateRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @Size(max = 100)
        String assignee,

        LocalDate dueDate,

        @NotNull
        KanbanPriority priority,

        @Size(max = 1000)
        String description
) {
}
