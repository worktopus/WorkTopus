package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.response.DashboardBoardResponse;
import com.example.worktopus.projects.dto.response.DashboardDayResponse;
import com.example.worktopus.projects.dto.response.DashboardResponse;
import com.example.worktopus.projects.dto.response.DashboardScheduleResponse;
import com.example.worktopus.projects.dto.response.KanbanCardResponse;
import com.example.worktopus.projects.dto.response.ProjectFileResponse;
import com.example.worktopus.projects.entity.KanbanCard;
import com.example.worktopus.projects.entity.KanbanStatus;
import com.example.worktopus.projects.repository.BoardFileRepository;
import com.example.worktopus.projects.repository.BoardRepository;
import com.example.worktopus.projects.repository.CalendarScheduleRepository;
import com.example.worktopus.projects.repository.KanbanCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;
    private final KanbanCardRepository kanbanCardRepository;
    private final CalendarScheduleRepository calendarScheduleRepository;

    @Override
    public DashboardResponse getDashboard(Long projectId) {
        long boardCount = boardRepository.countByProjectIdAndDeletedYn(projectId, "N");
        long fileCount = boardFileRepository.countProjectFiles(projectId);
        long kanbanCardCount = kanbanCardRepository.countByProjectIdAndDeletedYn(projectId, "N");
        long todoCount = kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(projectId, KanbanStatus.TODO, "N");
        long inProgressCount = kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(projectId, KanbanStatus.IN_PROGRESS, "N");
        long reviewCount = kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(projectId, KanbanStatus.REVIEW, "N");
        long doneCount = kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(projectId, KanbanStatus.DONE, "N");

        int completionRate = calculateCompletionRate(doneCount, kanbanCardCount);
        LocalDate today = LocalDate.now();
        List<KanbanCard> kanbanCards = kanbanCardRepository.findByProjectIdAndDeletedYnOrderByCreatedAtAsc(projectId, "N");
        List<DashboardScheduleResponse> upcomingSchedules = findUpcomingSchedules(projectId, today);

        return new DashboardResponse(
                boardCount,
                fileCount,
                kanbanCardCount,
                todoCount,
                inProgressCount,
                reviewCount,
                doneCount,
                completionRate,
                calculateRate(todoCount, kanbanCardCount),
                calculateRate(inProgressCount, kanbanCardCount),
                calculateRate(reviewCount, kanbanCardCount),
                calculateRate(doneCount, kanbanCardCount),
                filterKanbanCards(kanbanCards, KanbanStatus.TODO),
                filterKanbanCards(kanbanCards, KanbanStatus.IN_PROGRESS),
                filterKanbanCards(kanbanCards, KanbanStatus.REVIEW),
                findRecentBoards(projectId),
                findRecentFiles(projectId),
                upcomingSchedules,
                createCalendarDays(today, upcomingSchedules)
        );
    }

    private List<DashboardBoardResponse> findRecentBoards(Long projectId) {
        return boardRepository
                .findByProjectIdAndDeletedYnOrderByCreatedAtDesc(projectId, "N", PageRequest.of(0, 5))
                .stream()
                .map(DashboardBoardResponse::from)
                .toList();
    }

    private List<ProjectFileResponse> findRecentFiles(Long projectId) {
        return boardFileRepository.findRecentProjectFiles(projectId, PageRequest.of(0, 5));
    }

    private List<KanbanCardResponse> filterKanbanCards(
            List<KanbanCard> cards,
            KanbanStatus status
    ) {
        return cards.stream()
                .filter(card -> card.getStatus() == status)
                .limit(3)
                .map(KanbanCardResponse::from)
                .toList();
    }

    private List<DashboardScheduleResponse> findUpcomingSchedules(Long projectId, LocalDate today) {
        List<DashboardScheduleResponse> schedules = new ArrayList<>();

        schedules.addAll(calendarScheduleRepository
                .findByProjectIdAndDeletedYnAndStartDateGreaterThanEqualOrderByStartDateAscCreatedAtAsc(
                        projectId,
                        "N",
                        today,
                        PageRequest.of(0, 5)
                )
                .stream()
                .map(DashboardScheduleResponse::from)
                .toList());

        schedules.addAll(kanbanCardRepository
                .findByProjectIdAndDeletedYnAndDueDateGreaterThanEqualOrderByDueDateAscCreatedAtAsc(
                        projectId,
                        "N",
                        today,
                        PageRequest.of(0, 5)
                )
                .stream()
                .map(DashboardScheduleResponse::from)
                .toList());

        return schedules.stream()
                .sorted(Comparator.comparing(DashboardScheduleResponse::date))
                .limit(5)
                .toList();
    }

    private List<DashboardDayResponse> createCalendarDays(
            LocalDate today,
            List<DashboardScheduleResponse> upcomingSchedules
    ) {
        return java.util.stream.IntStream.range(0, 14)
                .mapToObj(today::plusDays)
                .map(date -> new DashboardDayResponse(
                        date,
                        toDayOfWeekLabel(date.getDayOfWeek()),
                        date.getDayOfWeek() == DayOfWeek.SATURDAY,
                        date.getDayOfWeek() == DayOfWeek.SUNDAY,
                        upcomingSchedules.stream()
                                .filter(schedule -> date.equals(schedule.date()))
                                .toList()
                ))
                .toList();
    }

    private int calculateCompletionRate(long doneCount, long totalCount) {
        return calculateRate(doneCount, totalCount);
    }

    private int calculateRate(long count, long totalCount) {
        if (totalCount <= 0) {
            return 0;
        }

        long rate = Math.round(count * 100.0 / totalCount);
        return (int) Math.max(0, Math.min(100, rate));
    }

    private String toDayOfWeekLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
