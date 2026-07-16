package com.example.WorkTopus.projects.dto.response;

import com.example.WorkTopus.projects.entity.CalendarSchedule;
import com.example.WorkTopus.projects.entity.KanbanCard;

import java.time.LocalDate;

public record DashboardScheduleResponse(
        String title,
        LocalDate date,
        String sourceType,
        String styleClass
) {

    public static DashboardScheduleResponse from(CalendarSchedule schedule) {
        return new DashboardScheduleResponse(
                schedule.getTitle(),
                schedule.getStartDate(),
                "CALENDAR",
                "schedule--blue"
        );
    }

    public static DashboardScheduleResponse from(KanbanCard card) {
        return new DashboardScheduleResponse(
                card.getTitle(),
                card.getDueDate(),
                "KANBAN",
                "schedule--red"
        );
    }
}
