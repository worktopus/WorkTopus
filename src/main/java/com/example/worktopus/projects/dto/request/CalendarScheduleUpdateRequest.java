package com.example.worktopus.projects.dto.request;

import com.example.worktopus.projects.entity.CalendarScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CalendarScheduleUpdateRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @NotNull
        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 1000)
        String description,

        @NotNull
        CalendarScheduleType type
) {
}
