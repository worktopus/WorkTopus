package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.projects.dto.response.DashboardResponse;
import com.example.WorkTopus.projects.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({
            "/projects/{projectId}",
            "/projects/{projectId}/dashboard",
            "/projects/{projectId}/boards/dashboard"
    })
    public ModelAndView dashboard(@PathVariable Long projectId) {

        DashboardResponse dashboard =
                dashboardService.getDashboard(projectId);

        ModelAndView mav =
                new ModelAndView("projects/dashboard");

        mav.addObject("projectId", projectId);
        mav.addObject("dashboard", dashboard);

        return mav;
    }
}