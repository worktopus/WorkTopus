package com.example.WorkTopus.projects.dto.response;

import java.util.List;

public record DashboardResponse(
        long boardCount,
        long fileCount,
        long kanbanCardCount,
        long todoCount,
        long inProgressCount,
        long reviewCount,
        long doneCount,
        int completionRate,
        int todoRate,
        int inProgressRate,
        int reviewRate,
        int doneRate,
        List<KanbanCardResponse> todoCards,
        List<KanbanCardResponse> inProgressCards,
        List<KanbanCardResponse> reviewCards,
        List<DashboardBoardResponse> recentBoards,
        List<ProjectFileResponse> recentFiles,
        List<DashboardScheduleResponse> upcomingSchedules,
        List<DashboardDayResponse> calendarDays
) {
}
