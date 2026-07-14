package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(Long projectId);
}
