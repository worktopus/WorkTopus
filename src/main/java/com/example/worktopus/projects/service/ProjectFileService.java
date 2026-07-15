package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.response.ProjectFileResponse;

import java.util.List;

public interface ProjectFileService {

    List<ProjectFileResponse> findProjectFiles(Long projectId);
}
