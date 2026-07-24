package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.manage.entity.ManageMember;
import com.example.WorkTopus.manage.repository.ManageMemberRepository;
import com.example.WorkTopus.projects.dto.response.*;
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

/**
 * 프로젝트 대시보드에 필요한 정보를 조회하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;
    private final KanbanCardRepository kanbanCardRepository;
    private final CalendarScheduleRepository calendarScheduleRepository;
    private final UserService userService;
    private final ManageMemberRepository manageMemberRepository;

    @Override
    public DashboardResponse getDashboard(Long projectId, String loginUserId) {
        // 프로젝트 참여 정보 조회
        ManageMember member = manageMemberRepository
                .findByWorkspaceIdAndUser_UserId(projectId, loginUserId)
                .orElseThrow(() ->
                        new IllegalArgumentException("프로젝트 참여 정보를 찾을 수 없습니다.")
                );

        // 프로젝트 전체 통계 조회
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

        // 프로젝트 진행률 계산
        int completionRate =
                calculateCompletionRate(doneCount, kanbanCardCount);

        // 칸반 카드 및 예정 일정 조회
        LocalDate today = LocalDate.now();

        List<KanbanCard> kanbanCards =
                kanbanCardRepository
                        .findByProjectIdAndDeletedYnOrderByCreatedAtAsc(
                                projectId,
                                "N"
                        );

        List<DashboardScheduleResponse> upcomingSchedules =
                findUpcomingSchedules(projectId, today);

        // 로그인 사용자 개인 통계 조회
        Users user = userService.findByUserId(loginUserId);
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

        // 대시보드 응답 생성
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
                member.getAssignedRole(),
                myDoneCount,
                myKanbanCardCount,
                myCompletionRate,

                filterKanbanCards(kanbanCards, KanbanStatus.TODO),
                filterKanbanCards(kanbanCards, KanbanStatus.IN_PROGRESS),
                filterKanbanCards(kanbanCards, KanbanStatus.REVIEW),
                findRecentBoards(projectId),
                findLatestNotice(projectId),
                findRecentFiles(projectId),
                upcomingSchedules,
                createCalendarDays(today, upcomingSchedules)
        );
    }

    // 최근 게시글 조회
    private List<DashboardBoardResponse> findRecentBoards(Long projectId) {
        return boardRepository
                .findByProjectIdAndDeletedYnOrderByCreatedAtDesc(projectId, "N", PageRequest.of(0, 5))
                .stream()
                .map(DashboardBoardResponse::from)
                .toList();
    }

    // 최신 공지 조회
    private NoticeResponse findLatestNotice(Long projectId) {
        return boardRepository
                .findFirstByProjectIdAndNoticeYnAndDeletedYnOrderByCreatedAtDesc(
                        projectId,
                        "Y",
                        "N"
                )
                .map(NoticeResponse::from)
                .orElse(null);
    }

    // 최근 업로드 파일 조회
    private List<ProjectFileResponse> findRecentFiles(Long projectId) {
        return boardFileRepository.findRecentProjectFiles(projectId, PageRequest.of(0, 5));
    }

    // 상태별 칸반 카드 최대 3건 조회
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

    // 캘린더 일정과 칸반 마감일을 통합하여 조회
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

        // 날짜순으로 정렬 후 최대 5건 반환
        return schedules.stream()
                .sorted(Comparator.comparing(DashboardScheduleResponse::date))
                .limit(5)
                .toList();
    }

    // 2주간 달력 데이터를 생성
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

    // 백분율 계산 (0~100%)
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
