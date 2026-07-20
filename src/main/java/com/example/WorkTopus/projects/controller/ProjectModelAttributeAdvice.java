package com.example.WorkTopus.projects.controller;

import com.example.WorkTopus.entity.Users;
import com.example.WorkTopus.repository.ProjectRepository;
import com.example.WorkTopus.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class ProjectModelAttributeAdvice {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @ModelAttribute
    public void addProjectInfo(
            Model model,
            Authentication authentication,
            HttpServletRequest request
    ) {
        model.addAttribute("isProjectOwner", false);

        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        Object attribute =
                request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        if (!(attribute instanceof Map<?, ?> variables)) {
            return;
        }

        Object projectIdValue = variables.get("projectId");

        if (projectIdValue == null) {
            return;
        }

        Long projectId = Long.valueOf(projectIdValue.toString());

        Users loginUser = userRepository
                .findByUserId(authentication.getName())
                .orElse(null);

        if (loginUser == null) {
            return;
        }

        boolean isProjectOwner = projectRepository.findById(projectId)
                .map(project ->
                        project.getOwner() != null
                                && project.getOwner().getUserNum()
                                .equals(loginUser.getUserNum())
                )
                .orElse(false);

        model.addAttribute("isProjectOwner", isProjectOwner);
    }
}