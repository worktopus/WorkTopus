package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(Long projectId, String loginUserId);
}
