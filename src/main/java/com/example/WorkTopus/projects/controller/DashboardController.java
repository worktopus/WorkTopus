package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.dto.response.DashboardResponse;
import com.example.WorkTopus.projects.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

/**
 * 프로젝트 대시보드 화면을 조회하는 컨트롤러.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({
            "/projects/{projectId}",
            "/projects/{projectId}/dashboard",
            "/projects/{projectId}/boards/dashboard"
    })
    public ModelAndView dashboard(@PathVariable Long projectId,
                                  Authentication authentication) {

        // 프로젝트 대시보드 정보 조회
        DashboardResponse dashboard =
                dashboardService.getDashboard(projectId,
                        authentication.getName());

        // 대시보드 화면에 데이터 전달
        ModelAndView mav =
                new ModelAndView("projects/dashboard");

        mav.addObject("projectId", projectId);
        mav.addObject("dashboard", dashboard);

        return mav;
    }
}