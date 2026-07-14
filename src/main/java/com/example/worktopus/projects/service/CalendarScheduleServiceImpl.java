package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.request.CalendarScheduleCreateRequest;
import com.example.worktopus.projects.dto.request.CalendarScheduleUpdateRequest;
import com.example.worktopus.projects.dto.response.CalendarScheduleResponse;
import com.example.worktopus.projects.entity.CalendarSchedule;
import com.example.worktopus.projects.repository.CalendarScheduleRepository;
import com.example.worktopus.projects.repository.KanbanCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarScheduleServiceImpl implements CalendarScheduleService {

    private final CalendarScheduleRepository calendarScheduleRepository;
    private final KanbanCardRepository kanbanCardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CalendarScheduleResponse> findProjectSchedules(Long projectId) {
        List<CalendarScheduleResponse> schedules = new ArrayList<>();

        schedules.addAll(calendarScheduleRepository
                .findByProjectIdAndDeletedYnOrderByStartDateAscCreatedAtAsc(projectId, "N")
                .stream()
                .map(CalendarScheduleResponse::from)
                .toList());

        schedules.addAll(kanbanCardRepository
                .findByProjectIdAndDeletedYnAndDueDateIsNotNullOrderByDueDateAscCreatedAtAsc(projectId, "N")
                .stream()
                .map(CalendarScheduleResponse::from)
                .toList());

        schedules.sort(Comparator.comparing(CalendarScheduleResponse::startDate));
        return schedules;
    }

    @Override
    public CalendarScheduleResponse create(Long projectId, CalendarScheduleCreateRequest request) {
        LocalDate endDate = normalizeEndDate(request.startDate(), request.endDate());
        validateDateRange(request.startDate(), endDate);

        CalendarSchedule schedule = new CalendarSchedule(
                projectId,
                request.title(),
                request.startDate(),
                endDate,
                request.description(),
                request.type()
        );

        return CalendarScheduleResponse.from(calendarScheduleRepository.save(schedule));
    }

    @Override
    public CalendarScheduleResponse update(Long projectId, Long scheduleId, CalendarScheduleUpdateRequest request) {
        CalendarSchedule schedule = getSchedule(projectId, scheduleId);
        LocalDate endDate = normalizeEndDate(request.startDate(), request.endDate());
        validateDateRange(request.startDate(), endDate);

        schedule.update(
                request.title(),
                request.startDate(),
                endDate,
                request.description(),
                request.type()
        );

        return CalendarScheduleResponse.from(schedule);
    }

    @Override
    public void delete(Long projectId, Long scheduleId) {
        CalendarSchedule schedule = getSchedule(projectId, scheduleId);
        schedule.delete();
    }

    private CalendarSchedule getSchedule(Long projectId, Long scheduleId) {
        return calendarScheduleRepository
                .findByIdAndProjectIdAndDeletedYn(scheduleId, projectId, "N")
                .orElseThrow(() -> new IllegalArgumentException("캘린더 일정을 찾을 수 없습니다."));
    }

    private LocalDate normalizeEndDate(LocalDate startDate, LocalDate endDate) {
        return endDate == null ? startDate : endDate;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }
    }
}
