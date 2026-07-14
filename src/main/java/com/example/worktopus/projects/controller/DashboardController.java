package com.example.worktopus.projects.controller;

import com.example.worktopus.projects.dto.response.DashboardResponse;
import com.example.worktopus.projects.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/boards/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ModelAndView dashboard(@PathVariable Long projectId) {
        DashboardResponse dashboard = dashboardService.getDashboard(projectId);

        ModelAndView mav = new ModelAndView("projects/dashboard");
        mav.addObject("projectId", projectId);
        mav.addObject("dashboard", dashboard);
        return mav;
    }
}
