package com.example.WorkTopus.admin.service;

import com.example.WorkTopus.admin.dto.response.AdminProjectResponse;
import com.example.WorkTopus.repository.ProjectMemberRepository;
import com.example.WorkTopus.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public Page<AdminProjectResponse> getProjects(
            String searchType,
            String keyword,
            Pageable pageable) {

        return projectRepository.searchProjects(
                        searchType,
                        keyword,
                        pageable
                )
                .map(project -> new AdminProjectResponse(

                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getOwner().getName(),
                        projectMemberRepository.countByProject_Id(project.getId()),
                        project.getCreatedAt()

                ));
    }

    public long getTotalProjectCount() {
        return projectRepository.count();
    }
}