package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.response.ProjectFileResponse;

import java.util.List;

public interface ProjectFileService {

    List<ProjectFileResponse> findProjectFiles(Long projectId);
}
