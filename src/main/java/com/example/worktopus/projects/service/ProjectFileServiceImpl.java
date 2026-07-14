package com.example.worktopus.projects.service;

import com.example.worktopus.projects.dto.response.ProjectFileResponse;
import com.example.worktopus.projects.repository.BoardFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectFileServiceImpl implements ProjectFileService {

    private final BoardFileRepository boardFileRepository;

    @Override
    public List<ProjectFileResponse> findProjectFiles(Long projectId) {
        return boardFileRepository.findProjectFiles(projectId);
    }
}
