package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.response.DashboardBoardResponse;
import com.example.WorkTopus.projects.dto.response.DashboardDayResponse;
import com.example.WorkTopus.projects.dto.response.DashboardResponse;
import com.example.WorkTopus.projects.dto.response.DashboardScheduleResponse;
import com.example.WorkTopus.projects.dto.response.KanbanCardResponse;
import com.example.WorkTopus.projects.dto.response.ProjectFileResponse;
import com.example.WorkTopus.projects.entity.KanbanCard;
import com.example.WorkTopus.projects.entity.KanbanStatus;
import com.example.WorkTopus.projects.repository.BoardFileRepository;
import com.example.WorkTopus.projects.repository.BoardRepository;
import com.example.WorkTopus.projects.repository.CalendarScheduleRepository;
import com.example.WorkTopus.projects.repository.KanbanCardRepository;
import com.example.WorkTopus.service.UserService;
import com.example.WorkTopus.entity.Users;
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
    private final UserService userService;

    @Override
    public DashboardResponse getDashboard(Long projectId, String userId) {
        long boardCount =
                boardRepository.countByProjectIdAndDeletedYn(projectId, "N");

        long fileCount =
                boardFileRepository.countProjectFiles(projectId);

        long kanbanCardCount =
                kanbanCardRepository.countByProjectIdAndDeletedYn(projectId, "N");

        long todoCount =
                kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(
                        projectId,
                        KanbanStatus.TODO,
                        "N"
                );

        long inProgressCount =
                kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(
                        projectId,
                        KanbanStatus.IN_PROGRESS,
                        "N"
                );

        long reviewCount =
                kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(
                        projectId,
                        KanbanStatus.REVIEW,
                        "N"
                );

        long doneCount =
                kanbanCardRepository.countByProjectIdAndStatusAndDeletedYn(
                        projectId,
                        KanbanStatus.DONE,
                        "N"
                );

        int completionRate =
                calculateCompletionRate(doneCount, kanbanCardCount);

        LocalDate today = LocalDate.now();

        List<KanbanCard> kanbanCards =
                kanbanCardRepository
                        .findByProjectIdAndDeletedYnOrderByCreatedAtAsc(
                                projectId,
                                "N"
                        );

        List<DashboardScheduleResponse> upcomingSchedules =
                findUpcomingSchedules(projectId, today);

        // 개인 데이터는 로그인 사용자 연동 전 임시 값
        Users user = userService.findByUserId(userId);
        String userName = user.getName();

        long myKanbanCardCount =
                kanbanCardRepository.countByProjectIdAndAssigneeAndDeletedYn(
                        projectId,
                        userName,
                        "N"
                );

        long myDoneCount =
                kanbanCardRepository.countByProjectIdAndAssigneeAndStatusAndDeletedYn(
                        projectId,
                        userName,
                        KanbanStatus.DONE,
                        "N"
                );

        int myCompletionRate =
                calculateRate(myDoneCount, myKanbanCardCount);

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

                userName,
                myDoneCount,
                myKanbanCardCount,
                myCompletionRate,

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
