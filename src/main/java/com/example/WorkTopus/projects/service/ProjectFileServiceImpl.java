package com.example.WorkTopus.projects.service;

import com.example.WorkTopus.projects.dto.response.ProjectFileResponse;
import com.example.WorkTopus.projects.repository.BoardFileRepository;
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
