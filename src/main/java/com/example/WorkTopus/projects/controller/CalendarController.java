package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.dto.request.CalendarScheduleCreateRequest;
import com.example.WorkTopus.projects.dto.request.CalendarScheduleUpdateRequest;
import com.example.WorkTopus.projects.dto.response.CalendarScheduleResponse;
import com.example.WorkTopus.projects.service.CalendarScheduleService;
import com.example.WorkTopus.projects.service.ProjectBoardAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

/**
 * 프로젝트 캘린더 화면 및 일정 CRUD 요청을 처리하는 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards/calendar")
public class CalendarController {

    private final CalendarScheduleService calendarScheduleService;
    private final ProjectBoardAccessService projectBoardAccessService;

    @GetMapping
    public ModelAndView calendar(@PathVariable Long projectId,
                                 Authentication authentication) {

        // 프로젝트 멤버 권한 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 캘린더 화면에 프로젝트 정보 전달
        ModelAndView mav = new ModelAndView("projects/calendar");
        mav.addObject("projectId", projectId);
        return mav;
    }

    @GetMapping("/schedules")
    @ResponseBody
    public List<CalendarScheduleResponse> schedules(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        // 프로젝트 멤버 권한 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 프로젝트 일정 목록 조회
        return calendarScheduleService.findProjectSchedules(projectId);
    }
    @PostMapping("/schedules")
    @ResponseBody
    public CalendarScheduleResponse create(
            @PathVariable Long projectId,
            @Valid @RequestBody CalendarScheduleCreateRequest request,
            Authentication authentication
    ) {
        // 프로젝트 멤버 권한 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 일정 등록
        return calendarScheduleService.create(projectId, request);
    }

    @PutMapping("/schedules/{scheduleId}")
    @ResponseBody
    public CalendarScheduleResponse update(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody CalendarScheduleUpdateRequest request,
            Authentication authentication
    ) {
        // 프로젝트 멤버 권한 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 일정 수정
        return calendarScheduleService.update(
                projectId,
                scheduleId,
                request
        );
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @ResponseBody
    public void delete(
            @PathVariable Long projectId,
            @PathVariable Long scheduleId,
            Authentication authentication
    ) {
        // 프로젝트 멤버 권한 확인
        projectBoardAccessService.validateMember(
                projectId,
                authentication.getName()
        );

        // 일정 삭제
        calendarScheduleService.delete(projectId, scheduleId);
    }
}
