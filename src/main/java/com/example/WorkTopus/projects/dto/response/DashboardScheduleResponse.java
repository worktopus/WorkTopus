package com.example.WorkTopus.projects.dto.response;

import com.example.WorkTopus.projects.entity.CalendarSchedule;
import com.example.WorkTopus.projects.entity.KanbanCard;

import java.time.LocalDate;

public record DashboardScheduleResponse(
        String title,
        LocalDate startDate,
        LocalDate endDate,
        String sourceType,
        String styleClass
) {

    public static DashboardScheduleResponse from(CalendarSchedule schedule) {
        return new DashboardScheduleResponse(
                schedule.getTitle(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                "CALENDAR",
                "schedule--blue"
        );
    }

    public static DashboardScheduleResponse from(KanbanCard card) {
        return new DashboardScheduleResponse(
                card.getTitle(),
                card.getDueDate(),
                card.getDueDate(),
                "KANBAN",
                "schedule--red"
        );
    }
}
