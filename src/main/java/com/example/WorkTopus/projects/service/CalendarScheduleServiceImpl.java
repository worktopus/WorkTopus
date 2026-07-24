package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.request.CalendarScheduleCreateRequest;
import com.example.WorkTopus.projects.dto.request.CalendarScheduleUpdateRequest;
import com.example.WorkTopus.projects.dto.response.CalendarScheduleResponse;
import com.example.WorkTopus.projects.entity.CalendarSchedule;
import com.example.WorkTopus.projects.repository.CalendarScheduleRepository;
import com.example.WorkTopus.projects.repository.KanbanCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 프로젝트 캘린더 일정에 대한 비즈니스 로직을 처리하는 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CalendarScheduleServiceImpl implements CalendarScheduleService {

    private final CalendarScheduleRepository calendarScheduleRepository;
    private final KanbanCardRepository kanbanCardRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CalendarScheduleResponse> findProjectSchedules(Long projectId) {
        // 캘린더 일정과 마감일이 있는 칸반 카드를 함께 조회
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

        // 날짜순으로 정렬하여 반환
        schedules.sort(Comparator.comparing(CalendarScheduleResponse::startDate));
        return schedules;
    }

    @Override
    public CalendarScheduleResponse create(Long projectId, CalendarScheduleCreateRequest request) {
        // 종료일 보정 및 기간 유효성 검증
        LocalDate endDate = normalizeEndDate(request.startDate(), request.endDate());
        validateDateRange(request.startDate(), endDate);

        // 일정 생성
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
        // 수정 대상 일정 조회
        CalendarSchedule schedule = getSchedule(projectId, scheduleId);

        // 종료일 보정 및 기간 유효성 검증
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
        // 삭제 대상 일정 조회
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
