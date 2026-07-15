package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.dto.request.CalendarScheduleCreateRequest;
import com.example.WorkTopus.projects.dto.request.CalendarScheduleUpdateRequest;
import com.example.WorkTopus.projects.dto.response.CalendarScheduleResponse;
import com.example.WorkTopus.projects.service.CalendarScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards/calendar")
public class CalendarController {

    private final CalendarScheduleService calendarScheduleService;

    @GetMapping
    public ModelAndView calendar(@PathVariable Long projectId) {
        ModelAndView mav = new ModelAndView("projects/calendar");
        mav.addObject("projectId", projectId);
        return mav;
    }

    @GetMapping("/schedules")
    @ResponseBody
    public List<CalendarScheduleResponse> schedules(@PathVariable Long projectId) {
        return calendarScheduleService.findProjectSchedules(projectId);
    }

    @PostMapping("/schedules")
    @ResponseBody
    public CalendarScheduleResponse create(
            @PathVariable Long projectId,
            @Valid @RequestBody CalendarScheduleCreateRequest request
    ) {
        return calendarScheduleService.create(projectId, request);
    }

    @PutMapping("/schedules/{scheduleId}")
    @ResponseBody
    public CalendarScheduleResponse update(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody CalendarScheduleUpdateRequest request
    ) {
        return calendarScheduleService.update(projectId, scheduleId, request);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @ResponseBody
    public void delete(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId
    ) {
        calendarScheduleService.delete(projectId, scheduleId);
    }
}
