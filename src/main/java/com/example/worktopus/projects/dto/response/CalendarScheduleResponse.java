package com.example.WorkTopus.projects.dto.response;

import com.example.WorkTopus.projects.entity.CalendarSchedule;
import com.example.WorkTopus.projects.entity.CalendarScheduleType;
import com.example.WorkTopus.projects.entity.KanbanCard;

import java.time.LocalDate;

public record CalendarScheduleResponse(
        Long id,
        Long projectId,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        CalendarScheduleType type,
        String typeValue,
        String sourceType,
        Long sourceId,
        boolean readOnly,
        String kanbanStatus
) {

    public static CalendarScheduleResponse from(CalendarSchedule schedule) {
        return new CalendarScheduleResponse(
                schedule.getId(),
                schedule.getProjectId(),
                schedule.getTitle(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getDescription(),
                schedule.getType(),
                schedule.getType().name().toLowerCase(),
                "CALENDAR",
                schedule.getId(),
                false,
                null
        );
    }

    public static CalendarScheduleResponse from(KanbanCard card) {
        return new CalendarScheduleResponse(
                null,
                card.getProjectId(),
                card.getTitle(),
                card.getDueDate(),
                card.getDueDate(),
                card.getDescription(),
                CalendarScheduleType.DEADLINE,
                CalendarScheduleType.DEADLINE.name().toLowerCase(),
                "KANBAN",
                card.getId(),
                true,
                card.getStatus().name()
        );
    }
}
