package com.example.WorkTopus.service;

import com.example.WorkTopus.dto.ProjectCreateForm;
import com.example.WorkTopus.entity.Projects;
import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public void createProject(ProjectCreateForm form, Users loginUser) {

        Projects project = new Projects();

        project.setName(form.getName());
        project.setDescription(form.getDescription());

        project.setOwner(loginUser);

        project.setInviteCode(generateInviteCode());

        projectRepository.save(project);
    }

    private String generateInviteCode() {

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        SecureRandom random = new SecureRandom();

        StringBuilder code = new StringBuilder();

        do {

            code.setLength(0);

            for (int i = 0; i < 6; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }

        } while (projectRepository.existsByInviteCode(code.toString()));

        return code.toString();
    }

    @Transactional(readOnly = true)
    public List<Projects> findProjectsByOwner(Users loginUser) {
        return projectRepository.findByOwnerOrderByCreatedAtDesc(loginUser);
    }

}