package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.request.CalendarScheduleCreateRequest;
import com.example.WorkTopus.projects.dto.request.CalendarScheduleUpdateRequest;
import com.example.WorkTopus.projects.dto.response.CalendarScheduleResponse;

import java.util.List;

public interface CalendarScheduleService {

    List<CalendarScheduleResponse> findProjectSchedules(Long projectId);

    CalendarScheduleResponse create(Long projectId, CalendarScheduleCreateRequest request);

    CalendarScheduleResponse update(Long projectId, Long scheduleId, CalendarScheduleUpdateRequest request);

    void delete(Long projectId, Long scheduleId);
}
